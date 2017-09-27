### use
1. `git clone ... `
1. open the project
1. create file `Main` like this :
```scala
object Main {
  implicit val nameMapping   = new NameMapping.Default {
  }
  // if you are not use mysql
  // you need add driver lib depend to build.sbt
  //your db
  implicit val databaseModel = new DataBaseModel {
    override val profileInstance = slick.jdbc.MySQLProfile // slick.jdbc.PostgresProfile ...
    override val jdbcDriver      = "com.mysql.cj.jdbc.Driver"
    override val url             = "jdbc:mysql://${IP}:${PORT}/${DB_NAME}?nullNamePatternMatchesAll=true"
    override val user            = Some("USER_NAME")
    override val password        = Some("PASSWORD")
  }
  // gen class class
  val genCaseClass  : GenCaseClass   = new GenCaseClass {
    override val outFileName: String      = "Tables"
    override val packageName: String      = <your case class package >
    // you want to ignore table
    override val ignoreTable: Seq[String] = Seq("tests")
  }
  val genQuillSchema: GenQuillSchema = new GenQuillSchema {
    //quill schema package name
    override val packageName      : String = <your quill schema package >
    override val modelsPackageName: String = genCaseClass.packageName
    override val traitName        : String = "QuillQuerySchema"
    override val style                     = ClassStyle.WithQuillContext("QuillContext")
    // you want to ignore table
    override val ignoreTable               = Nil
  }

  def main(args: Array[String]): Unit = {
    genCaseClass.run()
    genQuillSchema.run()
  }
}
```
