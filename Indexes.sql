-- ===== INDEXES =====
CREATE INDEX IX_Players_Name ON Players(Name);
CREATE INDEX IX_Player_CelestialBodies_PlayerID ON Player_CelestialBodies(PlayerID);
CREATE INDEX IX_Player_CelestialBodies_BodyID ON Player_CelestialBodies(BodyID);
CREATE INDEX IX_Alien_Attacks_TargetBodyID ON Alien_Attacks(TargetBodyID);
CREATE INDEX IX_Game_Sessions_PlayerID ON Game_Sessions(PlayerID);
CREATE INDEX IX_Player_Credits_Log_PlayerID ON Player_Credits_Log(PlayerID);
CREATE INDEX IX_Celestial_Bodies_Type ON Celestial_Bodies(Type);
CREATE INDEX IX_Player_Achievements_PlayerID ON Player_Achievements(PlayerID);
