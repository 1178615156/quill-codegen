import java.io.PrintWriter

import slick.jdbc.JdbcProfile
import slick.model.{Column, Model}
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

import slick.ast.ColumnOption

trait MakeModel {
  val profileInstance: JdbcProfile
  val jdbcDriver     : String
  val url            : String
  val user           : Option[String]
  val password       : Option[String]

  val ignoreInvalidDefaults: Boolean = true

  lazy          val dbFactory = profileInstance.api.Database
  lazy          val db        = dbFactory.forURL(url, driver = jdbcDriver, user = user.orNull, password = password.orNull, keepAliveConnection = true)
  implicit lazy val m: Model  = Await.result(db.run(profileInstance.createModel(None, ignoreInvalidDefaults)(ExecutionContext.global).withPinnedSession), Duration.Inf)

}

trait NameMapping {
  def column2fieldLiteral = false

  def tableName2className(s: String) = s.split("_").toList.map(headUpper).mkString("")

  def columnName2fieldName(s: String) = {
    val name: String =
      if(column2fieldLiteral) s else headLower(s.split("_").toList.map(headUpper).mkString(""))
    name match {
      case "class"  => s"`${name}`"
      case "object" => s"`${name}`"
      case "type"   => s"`${name}`"

      case name if name.forall(e => e.isLetterOrDigit || e == '_') => name

      case _ => s"`${name}`"
    }

  }

  def headLower(s: String) = s.head.toLower + s.tail

  def headUpper(s: String) = s.head.toUpper + s.tail

  def className2variableName(s: String) = headLower(s)
}

trait GenCaseClass {
  val packageName: String
  val outFileName: String


  def run()(implicit m: Model, nameMapping: NameMapping): Unit = {
    import nameMapping._
    val classes = m.tables.map { table =>
      val className = tableName2className(table.name.table)
      val fields = table.columns.map(column2filed)
      s"case class ${className}(${fields.mkString(",")})"
    }
    writeFile(classes)
  }

  private def column2filed(column: Column)(implicit nameMapping: NameMapping) = {
    import nameMapping._
    val fieldName = columnName2fieldName(column.name)
    val fieldType = if(column.nullable || column.options.contains(ColumnOption.AutoInc)
    ) s"Option[${column.tpe}]" else column.tpe
    s"""$fieldName:$fieldType"""
  }

  private def writeFile(classes: Seq[String]) = {
    val pw = new PrintWriter(if(outFileName.endsWith("scala")) outFileName else s"${outFileName}.scala")
    pw.write(s"package $packageName\n")
    pw.write(classes.mkString("\n"))
    pw.close()
  }
}

trait GenQuillSchema {
  val packageName     : String
  val tablePackageName: String
  val outFileName           : String         = "QuillQuerySchema"
  val traitName             : String         = "QuillQuerySchema"
  val importQuillContextName: Option[String] = None
  val withQuillContextName  : Option[String] = None


  def run()(implicit m: Model, nameMapping: NameMapping): Unit = {
    import nameMapping._
    val querySchemas = m.tables.map { table =>
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
    writeFile(querySchemas)
  }

  private def writeFile(querySchemas: Seq[String]) = {
    val pw = new PrintWriter(if(outFileName.endsWith("scala")) outFileName else s"${outFileName}.scala")
    pw.write(
      s"""package $packageName
         |
         |import ${tablePackageName}._
         |${importQuillContextName.map(e => s"import ${e}._").getOrElse("")}
         |
         |trait $traitName {
         |  ${withQuillContextName.map(e => s"self : $e =>").getOrElse("")}
         |${querySchemas.map("  " + _).mkString("\n")}
         |}
      """.stripMargin)
    pw.close()
  }
}

trait QuillCodeGen extends MakeModel {
  self =>

  implicit def nameMapping = new NameMapping {
  }

  val genCaseClass  : GenCaseClass
  val genQuillSchema: GenQuillSchema

  def run(): Unit = {
    genCaseClass.run()
    genQuillSchema.run()
  }
}

