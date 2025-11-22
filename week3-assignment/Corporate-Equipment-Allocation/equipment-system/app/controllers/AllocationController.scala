package controllers

import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import repository.{AllocationRepository, EmployeeRepository, EquipmentRepository}
import model.{ AllocationCreateRequest, AllocationCreatedEvent, AllocationReturnRequest, AllocationReturnedEvent, Condition, Role}
import security.RBAC
import model.AllocationModels._
import play.api.libs.json._
import play.api.libs.ws._
import model.errors._
import model.EventFormats._

import java.time.Instant

@Singleton
class AllocationController @Inject()(ws: WSClient, cc: ControllerComponents, repo: AllocationRepository, employeeRepo: EmployeeRepository, equipmentRepo: EquipmentRepository, secured: RBAC)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getAll: Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.RECEPTION_STAFF)).async { implicit request =>
      repo.findAll().map(list => Ok(Json.toJson(list)))
    }

  def getById(id: Int): Action[AnyContent] =
    secured.WithRole(List(Role.SYSTEM_ADMIN, Role.RECEPTION_STAFF)).async { implicit request =>
      repo.findById(id).map {
        case Some(a) => Ok(Json.toJson(a))
        case None => NotFound
      }
    }

  def create: Action[JsValue] =
    secured.WithRole(List(Role.RECEPTION_STAFF)).async(parse.json) { implicit request =>
      request.body.validate[AllocationCreateRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),

        req => {
          repo.create(req).flatMap {
            case Left(EmployeeNotFound) =>
              Future.successful(BadRequest("Employee does not exist"))

            case Left(EquipmentNotFound) =>
              Future.successful(BadRequest("Equipment does not exist"))

            case Left(EquipmentNotAvailable(id)) =>
              Future.successful(BadRequest(s"Equipment $id is not available"))

            case Right(newId) =>
              for {
                employee <- employeeRepo.findById(req.employeeId).map(_.get)
                equipment <- equipmentRepo.findById(req.equipmentId).map(_.get)
                equipmentType <- equipmentRepo.getEquipmentTypeById(equipment.typeId).map(_.get)

                _ <- ws.url("http://localhost:8082/produce-events")
                  .post(
                    Json.toJson(
                      AllocationCreatedEvent(
                        equipmentType = equipmentType.typeName,
                        equipmentSerial = equipment.serialNumber,
                        employeeId = employee.employeeId,
                        employeeName = employee.name,
                        employeeEmail = employee.email,
                        allocatedAt = Instant.now(),
                        expectedReturnAt = req.expectedReturnAt,
                      )
                    )
                  )
              } yield Created(Json.obj("allocationId" -> newId))
          }
        }
      )
    }

  def markReturned(id: Int): Action[JsValue] =
    secured.WithRole(List(Role.RECEPTION_STAFF)).async(parse.json) { implicit request =>
      request.body.validate[AllocationReturnRequest].fold(
        errors => Future.successful(BadRequest(JsError.toJson(errors))),

        req => {
          val returnedAt = Instant.now()

          repo.markReturned(id, req, returnedAt).flatMap {
            case Left(AllocationNotFound) =>
              Future.successful(NotFound)
            case Left(AlreadyReturned) =>
              Future.successful(BadRequest("Item is already returned"))

            case Right(allocation) =>
              for {
                employee <- employeeRepo.findById(allocation.employeeId).map(_.get)
                equipment <- equipmentRepo.findById(allocation.equipmentId).map(_.get)
                equipmentType <- equipmentRepo.getEquipmentTypeById(equipment.typeId).map(_.get)

                // Send event
                _ <- ws.url("http://localhost:8082/produce-events")
                  .post(
                    Json.toJson(
                      AllocationReturnedEvent(
                        equipmentType = equipmentType.typeName,
                        equipmentSerial = equipment.serialNumber,
                        employeeId = employee.employeeId,
                        employeeName = employee.name,
                        employeeEmail = employee.email,
                        returnedAt = returnedAt,
                        condition = req.conditionOnReturn.toString,
                        notes = req.notes
                      )
                    )
                  )
              } yield Ok(Json.obj("status" -> "returned"))
          }
        }
      )
    }


  def delete(id: Int): Action[AnyContent] =
    secured.WithRole(List(Role.RECEPTION_STAFF)).async { implicit request =>
      repo.delete(id).map {
        case 0 => NotFound
        case _ => Ok(Json.obj("status" -> "deleted"))
      }
    }
}
