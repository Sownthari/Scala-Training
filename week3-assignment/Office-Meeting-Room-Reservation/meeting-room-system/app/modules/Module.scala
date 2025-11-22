package modules

import com.google.inject.AbstractModule
import scheduler.MeetingSchedulers

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[MeetingSchedulers]).asEagerSingleton()
  }
}
