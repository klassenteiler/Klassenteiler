CREATE TABLE SchoolClass (
    id serial PRIMARY KEY,
    className varchar(20) NOT NULL,
    schoolName varchar(30),
    classSecret varchar(100) NOT NULL,
    encryptedPublicKey varchar(100) NOT NULL,
    teacherSecret varchar(100) NOT NULL,
    privateKey varchar(100) NOT NULL,
    surveyStatus varchar()
);

CREATE TABLE Student (
    id serial PRIMARY KEY,
    classId integer REFERENCES SchoolClass (id) ON DELETE CASCADE,
    hash varchar(100) NOT NULL,
    encryptedName varchar(100) NOT NULL,
    selfReported boolean NOT NULL,
    groupBelonging int DEFAULT NULL
);

CREATE TABLE Relationship (
    id serial PRIMARY KEY,
    sourceID integer REFERENCES SchoolClass (id) ON DELETE SET NULL,
    targetID integer REFERENCES SchoolClass (id) ON DELETE SET NULL
);

