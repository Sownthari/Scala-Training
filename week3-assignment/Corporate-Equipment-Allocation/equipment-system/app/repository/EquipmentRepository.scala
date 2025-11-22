package repository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider

import db.Tables._
import db.ColumnMappings._
import model.{Equipment, EquipmentType}
import model.Status.Status

class EquipmentRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  def getAllEquipmentTypes(): Future[Seq[EquipmentType]] =
    db.run(equipmentTypes.result)

  def getEquipmentTypeById(id: Int): Future[Option[EquipmentType]] =
    db.run(equipmentTypes.filter(_.typeId === id).result.headOption)
  def findAll(): Future[Seq[Equipment]] =
    db.run(equipment.result)

  def findBySerialNo(serialNo: String): Future[Option[Equipment]] =
    db.run(equipment.filter(_.serialNumber === serialNo).result.headOption)

  def findById(id: Int): Future[Option[Equipment]] =
    db.run(equipment.filter(_.equipmentId === id).result.headOption)

  def create(e: Equipment): Future[Int] =
    db.run((equipment returning equipment.map(_.equipmentId)) += e)

  def updateStatus(id: Int, newStatus: Status): Future[Int] =
    db.run(
      equipment
        .filter(_.equipmentId === id)
        .map(_.status)
        .update(newStatus)
    )

  def delete(id: Int): Future[Int] =
    db.run(equipment.filter(_.equipmentId === id).delete)
}
