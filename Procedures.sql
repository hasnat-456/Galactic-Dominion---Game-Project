CREATE PROCEDURE sp_RegisterPlayer
    @Name NVARCHAR(50),
    @PasswordHash NVARCHAR(255)
AS
BEGIN
    DECLARE @PlayerID VARCHAR(50) = @Name + '_' + CAST(ABS(CHECKSUM(NEWID())) % 100000 AS VARCHAR);
    INSERT INTO Players (PlayerID, Name, PasswordHash)
    VALUES (@PlayerID, @Name, @PasswordHash);
    SELECT @PlayerID AS NewPlayerID;
END;

CREATE PROCEDURE sp_LoginPlayer
    @PlayerID VARCHAR(50),
    @PasswordHash NVARCHAR(255)
AS
BEGIN
    IF EXISTS (SELECT 1 FROM Players WHERE PlayerID=@PlayerID AND PasswordHash=@PasswordHash)
    BEGIN
        INSERT INTO Player_Login_History (PlayerID, Success) VALUES (@PlayerID, 1);
        INSERT INTO Game_Sessions (PlayerID, Status) VALUES (@PlayerID, 'Active');
        SELECT 'Login Successful. Session Started.' AS Message;
    END
    ELSE
    BEGIN
        INSERT INTO Player_Login_History (PlayerID, Success) VALUES (@PlayerID, 0);
        SELECT 'Invalid credentials.' AS Message;
    END
END;

CREATE PROCEDURE sp_EarnCredits
    @PlayerID VARCHAR(50),
    @Amount INT,
    @Action NVARCHAR(100)
AS
BEGIN
    UPDATE Players
    SET Credits = Credits + @Amount,
        TotalEarnedCredits = TotalEarnedCredits + @Amount
    WHERE PlayerID = @PlayerID;

    INSERT INTO Player_Credits_Log (PlayerID, Action, Amount, NewTotal)
    VALUES (@PlayerID, @Action, @Amount, (SELECT Credits FROM Players WHERE PlayerID=@PlayerID));
END;

CREATE PROCEDURE sp_ConductResearch
    @PlayerID VARCHAR(50),
    @PointsEarned INT
AS
BEGIN
    DECLARE @TechID INT;
    SELECT TOP 1 @TechID = TechID FROM Player_Technology_Progress WHERE PlayerID=@PlayerID;

    UPDATE Technology
    SET ResearchPoints = ResearchPoints + @PointsEarned
    WHERE TechID = @TechID;

    IF (SELECT ResearchPoints FROM Technology WHERE TechID=@TechID) >= 300
    BEGIN
        UPDATE Technology
        SET TechLevel = TechLevel + 1,
            ResearchPoints = ResearchPoints - 300
        WHERE TechID = @TechID;

        UPDATE Players
        SET TechnologyLevel = (SELECT TechLevel FROM Technology WHERE TechID=@TechID)
        WHERE PlayerID = @PlayerID;
    END
END;

CREATE PROCEDURE sp_RecordAlienAttack
    @Species NVARCHAR(100),
    @AttackPower INT,
    @TargetBodyID INT,
    @Success BIT,
    @DamagePop INT,
    @DamageRes INT
AS
BEGIN
    INSERT INTO Alien_Attacks (Species, AttackPower, TargetBodyID, Success, DamagePopulation, DamageResources)
    VALUES (@Species, @AttackPower, @TargetBodyID, @Success, @DamagePop, @DamageRes);
END;

CREATE PROCEDURE sp_StartGameSession
    @PlayerID VARCHAR(50)
AS
BEGIN
    INSERT INTO Game_Sessions (PlayerID, Status) VALUES (@PlayerID, 'Active');
END;

CREATE PROCEDURE sp_EndGameSession
    @PlayerID VARCHAR(50)
AS
BEGIN
    UPDATE Game_Sessions
    SET EndTime = GETDATE(), Status = 'Completed'
    WHERE PlayerID = @PlayerID AND Status = 'Active';
END;

DROP PROCEDURE IF EXISTS sp_CheckGalaxyVictory;
GO

CREATE PROCEDURE sp_CheckGalaxyVictory
    @PlayerID VARCHAR(50)
AS
BEGIN
    DECLARE @ConqueredCount INT, @TotalBodies INT, @TechLevel INT, @Credits INT, @SessionID INT;

    -- Get active session for player
    SELECT TOP 1 @SessionID = SessionID 
    FROM Game_Sessions 
    WHERE PlayerID = @PlayerID AND Status = 'Active';

    -- Count conquered bodies
    SELECT @ConqueredCount = COUNT(*) 
    FROM Player_CelestialBodies pcb
    JOIN Celestial_Bodies cb ON pcb.BodyID = cb.BodyID
    WHERE pcb.PlayerID = @PlayerID AND cb.Conquered = 1;

    -- Total bodies owned
    SELECT @TotalBodies = COUNT(*) 
    FROM Player_CelestialBodies 
    WHERE PlayerID = @PlayerID;

    -- Get player stats
    SELECT @TechLevel = TechnologyLevel, @Credits = Credits 
    FROM Players 
    WHERE PlayerID = @PlayerID;

    -- Check victory conditions
    IF (@TotalBodies > 0 AND @ConqueredCount >= @TotalBodies * 0.8
        AND @TechLevel >= 10 AND @Credits >= 5000)
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM Player_Achievements 
            WHERE PlayerID = @PlayerID AND Title = 'Galaxy Conqueror' AND SessionID = @SessionID
        )
        BEGIN
            INSERT INTO Player_Achievements (PlayerID, SessionID, Title, Description)
            VALUES (@PlayerID, @SessionID, 'Galaxy Conqueror', 'Conquered the galaxy in this session.');
        END

        UPDATE Game_Sessions
        SET EndTime = GETDATE(), Status = 'Completed'
        WHERE SessionID = @SessionID;

        SELECT 'Galaxy Victory Achieved!' AS Message;
    END
    ELSE
    BEGIN
        SELECT 'Victory conditions not met yet.' AS Message;
    END
END;
GO





