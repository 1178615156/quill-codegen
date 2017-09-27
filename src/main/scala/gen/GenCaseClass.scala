package gen

import java.io.PrintWriter

import slick.ast.ColumnOption
import slick.model.{Column, Model}

trait GenCaseClass {
  val packageName: String
  val outFileName: String
  val ignoreTable: Seq[String] = Nil

  def run()(implicit dbm: DataBaseModel, nameMapping: NameMapping): Unit = {
    import nameMapping._
    import dbm.m
    val classes = m.tables.filterNot(t => ignoreTable.contains(t.name.table)).map { table =>
      val className = tableName2className(table.name.table)
      val fields = table.columns.map(column2filed)
      s"case class ${className}(${fields.mkString(",")})"
    }
    writeFile(classes)
  }

  private def column2filed(column: Column)(implicit dbm: DataBaseModel, nameMapping: NameMapping) = {
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