UPDATE rooms SET latitude=16.0368457, longitude=108.2120095 WHERE id=43;
UPDATE rooms SET latitude=16.0368457, longitude=108.2120095 WHERE id=41;
UPDATE rooms SET latitude=16.0646402, longitude=108.2191943 WHERE id=42;
UPDATE rooms SET latitude=16.0131945, longitude=108.1911514 WHERE id=45;
UPDATE rooms SET latitude=16.0189563, longitude=108.2070896 WHERE id=44;
UPDATE rooms SET latitude=16.0574029, longitude=108.2116512 WHERE id=46;
UPDATE rooms SET latitude=16.0539593, longitude=108.157077 WHERE id=47;
UPDATE rooms SET latitude=16.0632427, longitude=108.154946 WHERE id=48;
UPDATE rooms SET latitude=16.0476588, longitude=108.2347747 WHERE id=50;
UPDATE rooms SET latitude=16.0229784, longitude=108.2468641 WHERE id=51;
UPDATE rooms SET latitude=16.0675696, longitude=108.2015043 WHERE id=49;
UPDATE rooms SET latitude=16.0368457, longitude=108.2120095 WHERE id=53;
UPDATE rooms SET latitude=16.0131945, longitude=108.1911514 WHERE id=54;
UPDATE rooms SET latitude=16.0731131, longitude=108.1745569 WHERE id=52;
UPDATE rooms SET latitude=16.0540564, longitude=108.2054825 WHERE id=55;
UPDATE rooms SET latitude=16.0238326, longitude=108.2125453 WHERE id=56;
UPDATE rooms SET latitude=16.0196238, longitude=108.2547698 WHERE id=57;
UPDATE rooms SET latitude=16.0731131, longitude=108.1745569 WHERE id=58;
UPDATE rooms SET latitude=16.0731131, longitude=108.1745569 WHERE id=59;

ALTER TABLE rooms
  MODIFY latitude DOUBLE NOT NULL,
  MODIFY longitude DOUBLE NOT NULL;
