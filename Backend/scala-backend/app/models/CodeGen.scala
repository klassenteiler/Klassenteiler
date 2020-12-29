package models

// turns database configuration into scala code
// 1. run db on localhost
// 2. Create Tables in postgres db with code in ../database/sql/create_tables.sql
// 3. run this file by starting the sbt shell with 'sbt' and running 'runMain models.CodeGen'

object CodeGen extends App {
    slick.codegen.SourceCodeGenerator.run(
        "slick.jdbc.PostgresProfile",
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost/test_db?user=root&password=test_password",
        "/home/anton/Desktop/Klassentrenner/Backend/scala-backend/app/", 
        "models", None, None, true, false
    )
}