package gen

import java.io.PrintWriter

import slick.model.Model

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

trait GenQuillSchema {
  val packageName      : String
  val modelsPackageName: String
  val style            : ClassStyle
  val outFileName: String      = "QuillQuerySchema"
  val traitName  : String      = "QuillQuerySchema"
  val ignoreTable: Seq[String] = Nil

  def run()(implicit dbm: DataBaseModel, nameMapping: NameMapping): Unit = {
    val classText = mkClass
    val pw = new PrintWriter(if(outFileName.endsWith("scala")) outFileName else s"${outFileName}.scala")
    pw.write(classText)
    pw.close()
  }

  def body(implicit dbm: DataBaseModel, nameMapping: NameMapping): Seq[String] = {
    import nameMapping._
    import dbm.m
    val querySchemas = m.tables.filterNot(t => ignoreTable.contains(t.name.table)).map { table =>
      val tableName = table.name.table
      val caseClassName = tableName2className(tableName)
      val variableName = className2variableName(caseClassName)

      val columnMappings = table.columns.map { column =>
        val columnName = column.name
        val fieldName = columnName2fieldName(columnName)
        s"""_.$fieldName -> "$columnName" """
      }
      val body = s"""quote(querySchema[$caseClassName]("$tableName",${columnMappings.mkString(",")}))"""
      s"final val $variableName = $body"
    }
    querySchemas
  }

  def mkClass(implicit dbm: DataBaseModel, nameMapping: NameMapping): String = {
    val body = this.body
    style match {
      case ClassStyle.WithQuillContext(ctxCLass)  =>
        s"""package $packageName
           |import $modelsPackageName._
           |
           |trait $traitName{
           |  self : $ctxCLass =>
           |
           |${body.map("  " + _).mkString("\n")}
           |}
         """.stripMargin
      case ClassStyle.ImportQuillContext(ctxName) =>
        s"""package $packageName
           |import $modelsPackageName._
           |import $ctxName._
           |
           |trait $traitName{
           |${body.map("  " + _).mkString("\n")}
           |}
         """.stripMargin
    }

  }

}