package modules

import com.google.inject.AbstractModule
import scheduler.OverdueScheduler

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[OverdueScheduler]).asEagerSingleton()
  }
}
