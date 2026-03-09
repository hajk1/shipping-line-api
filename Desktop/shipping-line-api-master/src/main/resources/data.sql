-- Ports
INSERT INTO ports (unlocode, name, country, created_at, updated_at)
VALUES ('AEJEA', 'Jebel Ali', 'UAE', NOW(), NOW()),
       ('CNSHA', 'Shanghai', 'China', NOW(), NOW()),
       ('NLRTM', 'Rotterdam', 'Netherlands', NOW(), NOW()),
       ('SGSIN', 'Singapore', 'Singapore', NOW(), NOW()),
       ('USNYC', 'New York', 'USA', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Vessel
INSERT INTO vessels (name, imo_number, capacity_teu, created_at, updated_at)
VALUES ('MV Freight Star', '1234567', 5000, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Containers
INSERT INTO containers (container_code, size, type, created_at, updated_at)
VALUES ('MSCU1234567', 'TWENTY_FOOT', 'DRY', NOW(), NOW()),
       ('MSCU7654321', 'FORTY_FOOT', 'REEFER', NOW(), NOW()),
       ('HLCU9988776', 'TWENTY_FOOT', 'OPEN_TOP', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Agents
INSERT INTO agents (name, email, commission_percent, type, active, created_at, updated_at)
VALUES ('Alice Johnson', 'alice@freightops.com', 5.00, 'INTERNAL', true, NOW(), NOW()),
       ('Bob Smith', 'bob@externalagents.com', 7.50, 'EXTERNAL', true, NOW(), NOW()),
       ('Carol White', 'carol@freightops.com', 4.25, 'INTERNAL', false, NOW(), NOW())
ON CONFLICT DO NOTHING;
