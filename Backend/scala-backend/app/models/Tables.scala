package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.PostgresProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Relationship.schema ++ Schoolclass.schema ++ Student.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Relationship
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param sourceid Database column sourceid SqlType(int4), Default(None)
   *  @param targetid Database column targetid SqlType(int4), Default(None) */
  case class RelationshipRow(id: Int, sourceid: Option[Int] = None, targetid: Option[Int] = None)
  /** GetResult implicit for fetching RelationshipRow objects using plain SQL queries */
  implicit def GetResultRelationshipRow(implicit e0: GR[Int], e1: GR[Option[Int]]): GR[RelationshipRow] = GR{
    prs => import prs._
    RelationshipRow.tupled((<<[Int], <<?[Int], <<?[Int]))
  }
  /** Table description of table relationship. Objects of this class serve as prototypes for rows in queries. */
  class Relationship(_tableTag: Tag) extends profile.api.Table[RelationshipRow](_tableTag, "relationship") {
    def * = (id, sourceid, targetid) <> (RelationshipRow.tupled, RelationshipRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), sourceid, targetid)).shaped.<>({r=>import r._; _1.map(_=> RelationshipRow.tupled((_1.get, _2, _3)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column sourceid SqlType(int4), Default(None) */
    val sourceid: Rep[Option[Int]] = column[Option[Int]]("sourceid", O.Default(None))
    /** Database column targetid SqlType(int4), Default(None) */
    val targetid: Rep[Option[Int]] = column[Option[Int]]("targetid", O.Default(None))

    /** Foreign key referencing Schoolclass (database name relationship_sourceid_fkey) */
    lazy val schoolclassFk1 = foreignKey("relationship_sourceid_fkey", sourceid, Schoolclass)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.SetNull)
    /** Foreign key referencing Schoolclass (database name relationship_targetid_fkey) */
    lazy val schoolclassFk2 = foreignKey("relationship_targetid_fkey", targetid, Schoolclass)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.SetNull)
  }
  /** Collection-like TableQuery object for table Relationship */
  lazy val Relationship = new TableQuery(tag => new Relationship(tag))

  /** Entity class storing rows of table Schoolclass
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param classname Database column classname SqlType(varchar), Length(20,true)
   *  @param schoolname Database column schoolname SqlType(varchar), Length(30,true), Default(None)
   *  @param classsecret Database column classsecret SqlType(varchar), Length(100,true)
   *  @param encryptedpublickey Database column encryptedpublickey SqlType(varchar), Length(100,true)
   *  @param teachersecret Database column teachersecret SqlType(varchar), Length(100,true)
   *  @param privatekey Database column privatekey SqlType(varchar), Length(100,true)
   *  @param surveystatus Database column surveystatus SqlType(varchar), Length(11,true), Default(None) */
  case class SchoolclassRow(id: Int, classname: String, schoolname: Option[String] = None, classsecret: String, encryptedpublickey: String, teachersecret: String, privatekey: String, surveystatus: Option[String] = None)
  /** GetResult implicit for fetching SchoolclassRow objects using plain SQL queries */
  implicit def GetResultSchoolclassRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[String]]): GR[SchoolclassRow] = GR{
    prs => import prs._
    SchoolclassRow.tupled((<<[Int], <<[String], <<?[String], <<[String], <<[String], <<[String], <<[String], <<?[String]))
  }
  /** Table description of table schoolclass. Objects of this class serve as prototypes for rows in queries. */
  class Schoolclass(_tableTag: Tag) extends profile.api.Table[SchoolclassRow](_tableTag, "schoolclass") {
    def * = (id, classname, schoolname, classsecret, encryptedpublickey, teachersecret, privatekey, surveystatus) <> (SchoolclassRow.tupled, SchoolclassRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(classname), schoolname, Rep.Some(classsecret), Rep.Some(encryptedpublickey), Rep.Some(teachersecret), Rep.Some(privatekey), surveystatus)).shaped.<>({r=>import r._; _1.map(_=> SchoolclassRow.tupled((_1.get, _2.get, _3, _4.get, _5.get, _6.get, _7.get, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column classname SqlType(varchar), Length(20,true) */
    val classname: Rep[String] = column[String]("classname", O.Length(20,varying=true))
    /** Database column schoolname SqlType(varchar), Length(30,true), Default(None) */
    val schoolname: Rep[Option[String]] = column[Option[String]]("schoolname", O.Length(30,varying=true), O.Default(None))
    /** Database column classsecret SqlType(varchar), Length(100,true) */
    val classsecret: Rep[String] = column[String]("classsecret", O.Length(100,varying=true))
    /** Database column encryptedpublickey SqlType(varchar), Length(100,true) */
    val encryptedpublickey: Rep[String] = column[String]("encryptedpublickey", O.Length(100,varying=true))
    /** Database column teachersecret SqlType(varchar), Length(100,true) */
    val teachersecret: Rep[String] = column[String]("teachersecret", O.Length(100,varying=true))
    /** Database column privatekey SqlType(varchar), Length(100,true) */
    val privatekey: Rep[String] = column[String]("privatekey", O.Length(100,varying=true))
    /** Database column surveystatus SqlType(varchar), Length(11,true), Default(None) */
    val surveystatus: Rep[Option[String]] = column[Option[String]]("surveystatus", O.Length(11,varying=true), O.Default(None))
  }
  /** Collection-like TableQuery object for table Schoolclass */
  lazy val Schoolclass = new TableQuery(tag => new Schoolclass(tag))

  /** Entity class storing rows of table Student
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param classid Database column classid SqlType(int4), Default(None)
   *  @param hash Database column hash SqlType(varchar), Length(100,true)
   *  @param encryptedname Database column encryptedname SqlType(varchar), Length(100,true)
   *  @param selfreported Database column selfreported SqlType(bool)
   *  @param groupbelonging Database column groupbelonging SqlType(int4), Default(None) */
  case class StudentRow(id: Int, classid: Option[Int] = None, hash: String, encryptedname: String, selfreported: Boolean, groupbelonging: Option[Int] = None)
  /** GetResult implicit for fetching StudentRow objects using plain SQL queries */
  implicit def GetResultStudentRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[String], e3: GR[Boolean]): GR[StudentRow] = GR{
    prs => import prs._
    StudentRow.tupled((<<[Int], <<?[Int], <<[String], <<[String], <<[Boolean], <<?[Int]))
  }
  /** Table description of table student. Objects of this class serve as prototypes for rows in queries. */
  class Student(_tableTag: Tag) extends profile.api.Table[StudentRow](_tableTag, "student") {
    def * = (id, classid, hash, encryptedname, selfreported, groupbelonging) <> (StudentRow.tupled, StudentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), classid, Rep.Some(hash), Rep.Some(encryptedname), Rep.Some(selfreported), groupbelonging)).shaped.<>({r=>import r._; _1.map(_=> StudentRow.tupled((_1.get, _2, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column classid SqlType(int4), Default(None) */
    val classid: Rep[Option[Int]] = column[Option[Int]]("classid", O.Default(None))
    /** Database column hash SqlType(varchar), Length(100,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(100,varying=true))
    /** Database column encryptedname SqlType(varchar), Length(100,true) */
    val encryptedname: Rep[String] = column[String]("encryptedname", O.Length(100,varying=true))
    /** Database column selfreported SqlType(bool) */
    val selfreported: Rep[Boolean] = column[Boolean]("selfreported")
    /** Database column groupbelonging SqlType(int4), Default(None) */
    val groupbelonging: Rep[Option[Int]] = column[Option[Int]]("groupbelonging", O.Default(None))

    /** Foreign key referencing Schoolclass (database name student_classid_fkey) */
    lazy val schoolclassFk = foreignKey("student_classid_fkey", classid, Schoolclass)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Student */
  lazy val Student = new TableQuery(tag => new Student(tag))
}
