CREATE TRIGGER trg_LogCreditsUpdate
ON Players
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(Credits)
    BEGIN
        INSERT INTO Player_Credits_Log (PlayerID, Action, Amount, NewTotal)
        SELECT 
            i.PlayerID,
            'Credits Updated',
            (i.Credits - d.Credits),
            i.Credits
        FROM inserted i
        JOIN deleted d ON i.PlayerID = d.PlayerID;
    END
END;


CREATE TRIGGER trg_PlanetVictory
ON Planets
AFTER UPDATE
AS
BEGIN
    UPDATE cb
    SET cb.Conquered = 1
    FROM Celestial_Bodies cb
    JOIN inserted p ON cb.BodyID = p.BodyID
    WHERE 
        (p.Population >= 1000 AND p.Resources >= 800 AND p.Defense >= 500)
        OR (p.Resources >= 1200 AND p.Scanned = 1)
        OR (p.Terraformed = 1 AND p.Defense >= 400);
END;

CREATE TRIGGER trg_EndGameSession
ON Player_Login_History
AFTER UPDATE
AS
BEGIN
    UPDATE gs
    SET gs.EndTime = GETDATE(),
        gs.Status = 'Completed'
    FROM Game_Sessions gs
    JOIN inserted i ON i.PlayerID = gs.PlayerID
    WHERE i.LogoutTime IS NOT NULL AND gs.Status = 'Active';
END;



CREATE TRIGGER trg_GalaxyVictory_Auto
ON Players
AFTER UPDATE
AS
BEGIN
    DECLARE @PlayerID VARCHAR(50);
    SELECT @PlayerID = i.PlayerID FROM inserted i;
    EXEC sp_CheckGalaxyVictory @PlayerID = @PlayerID;
END;
GO


