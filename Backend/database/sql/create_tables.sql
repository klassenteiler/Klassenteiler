CREATE TABLE SchoolClass (
    id serial PRIMARY KEY,
    className varchar(50) NOT NULL,
    schoolName varchar(50),
    classSecret varchar(100) NOT NULL,
    PublicKey varchar(600) NOT NULL,
    teacherSecret varchar(100) NOT NULL,
    encryptedPrivateKey varchar(2000) NOT NULL,
    surveyStatus Int DEFAULT 0 
);

CREATE TABLE Student (
    id serial PRIMARY KEY,
    classId integer REFERENCES SchoolClass (id) ON DELETE CASCADE,
    hashedName varchar(100) NOT NULL,
    encryptedName varchar(600) NOT NULL,
    selfReported boolean NOT NULL,
    groupBelonging int DEFAULT NULL
);

CREATE TABLE Relationship (
    id serial PRIMARY KEY,
    classId integer REFERENCES SchoolClass (id) ON DELETE CASCADE,
    sourceID integer REFERENCES Student (id) ON DELETE CASCADE,
    targetID integer REFERENCES Student (id) ON DELETE CASCADE
);

