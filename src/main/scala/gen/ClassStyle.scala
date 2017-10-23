package gen

trait ClassStyle

object ClassStyle {

  /** it will gen like this
    * import [[ctxName]]._
    * trait ${traitName} {
    *
    * }
    */
  case class ImportQuillContext(ctxName: String) extends ClassStyle


  /** it will gen like this
    * trait ${traitName} {
    * self : [[ctxCLass]] =>
    * }
    */
  case class WithQuillContext(ctxCLass: String) extends ClassStyle

}