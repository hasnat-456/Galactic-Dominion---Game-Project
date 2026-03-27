CREATE TABLE Players (
    PlayerID VARCHAR(50) PRIMARY KEY,
    Name NVARCHAR(50) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    TechnologyLevel INT DEFAULT 1,
    Credits INT DEFAULT 500,
    TotalEarnedCredits INT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE()
);

CREATE TABLE Celestial_Bodies (
    BodyID INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Type NVARCHAR(50) NOT NULL,  -- Planet, Nebula, Asteroid
    Conquered BIT DEFAULT 0
);

CREATE TABLE Planets (
    PlanetID INT IDENTITY(1,1) PRIMARY KEY,
    BodyID INT FOREIGN KEY REFERENCES Celestial_Bodies(BodyID),
    Population INT,
    Resources INT,
    Defense INT,
    Habitable BIT,
    Terraformed BIT,
    Scanned BIT
);

CREATE TABLE Nebulas (
    NebulaID INT IDENTITY(1,1) PRIMARY KEY,
    BodyID INT FOREIGN KEY REFERENCES Celestial_Bodies(BodyID),
    Density INT,
    RadiationLevel INT,
    VisibilityReduction BIT,
    Mapped BIT,
    ResourcesExtracted BIT
);

CREATE TABLE Asteroids (
    AsteroidID INT IDENTITY(1,1) PRIMARY KEY,
    BodyID INT FOREIGN KEY REFERENCES Celestial_Bodies(BodyID),
    Mass FLOAT,
    Diameter FLOAT,
    OrbitRadius FLOAT,
    Velocity FLOAT,
    Hazardous BIT,
    Mined BIT,
    OrbitStabilized BIT
);

CREATE TABLE Technology (
    TechID INT IDENTITY(1,1) PRIMARY KEY,
    ResearchPoints INT DEFAULT 0,
    TechLevel INT DEFAULT 1,
    NewPlanetsDiscovered BIT DEFAULT 0,
    AdvancedResourcesFound BIT DEFAULT 0
);

CREATE TABLE Player_Technology_Progress (
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    TechID INT FOREIGN KEY REFERENCES Technology(TechID),
    PRIMARY KEY (PlayerID, TechID)
);

CREATE TABLE Player_CelestialBodies (
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    BodyID INT FOREIGN KEY REFERENCES Celestial_Bodies(BodyID),
    AddedDate DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (PlayerID, BodyID)
);

CREATE TABLE Alien_Attacks (
    AttackID INT IDENTITY(1,1) PRIMARY KEY,
    Species NVARCHAR(100),
    AttackPower INT,
    TargetBodyID INT FOREIGN KEY REFERENCES Celestial_Bodies(BodyID),
    Success BIT,
    DamagePopulation INT,
    DamageResources INT,
    AttackDate DATETIME DEFAULT GETDATE()
);

CREATE TABLE Player_Resources (
    ResourceID INT IDENTITY(1,1) PRIMARY KEY,
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    ResourceType NVARCHAR(50),
    Quantity INT,
    UpdatedAt DATETIME DEFAULT GETDATE()
);

CREATE TABLE Player_Credits_Log (
    LogID INT IDENTITY(1,1) PRIMARY KEY,
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    Action NVARCHAR(100),
    Amount INT,
    NewTotal INT,
    LogDate DATETIME DEFAULT GETDATE()
);

CREATE TABLE Game_Sessions (
    SessionID INT IDENTITY(1,1) PRIMARY KEY,
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    StartTime DATETIME DEFAULT GETDATE(),
    EndTime DATETIME NULL,
    Status NVARCHAR(20) DEFAULT 'Active'
);

CREATE TABLE Player_Achievements (
    AchievementID INT IDENTITY(1,1) PRIMARY KEY,
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    Title NVARCHAR(100),
    Description NVARCHAR(255),
    DateEarned DATETIME DEFAULT GETDATE()
);

CREATE TABLE Player_Login_History (
    LoginID INT IDENTITY(1,1) PRIMARY KEY,
    PlayerID VARCHAR(50) FOREIGN KEY REFERENCES Players(PlayerID),
    LoginTime DATETIME DEFAULT GETDATE(),
    LogoutTime DATETIME NULL,
    Success BIT DEFAULT 1
);



