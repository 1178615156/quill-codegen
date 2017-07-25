
```scala
object Main {

  val gen = new QuillCodeGen {
    //your db
    override val profileInstance = slick.jdbc.MySQLProfile
    override val jdbcDriver      = "com.mysql.cj.jdbc.Driver"
    override val url             = "jdbc:mysql://${IP}:${PORT}/${DB_NAME}?nullNamePatternMatchesAll=true"
    override val user            = Some("USER_NAME")
    override val password        = Some("PASSWORD")


    override val genCaseClass  : GenCaseClass   = new GenCaseClass {
      override val outFileName: String = "Tables" 
      override val packageName: String = "hello.world.tables"
    }
    
    override val genQuillSchema: GenQuillSchema = new GenQuillSchema {
      //quill schema package name 
      override val packageName     : String = "hello.world.tables"
      override val tablePackageName: String = genCaseClass.packageName
      
      /** it will gen like this
        * trait $name {
        *   self : QuillConteext =>
        * }
        */
      override val withQuillContextName: Option[String] = Some("QuillContext")
      
      /** it will gen like this
        * import ctx._ 
        *  
        */
      override val importQuillContextName = None 
    }

  }

  def main(args: Array[String]): Unit = {
    gen.run()
  }
}
```
