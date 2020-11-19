package seija.ui2

import seija.data.SList

trait IBehavior {
  def HandleEvent(evData:SList):Unit = {}
}
