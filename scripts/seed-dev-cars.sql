INSERT INTO cars (brand, model, fuel_type, seating_capacity, daily_rate, status, year, license_plate, description)
SELECT brand, model, fuel_type, seating_capacity, daily_rate, status, year, license_plate, description
FROM (
    SELECT 'Toyota'         AS brand, 'Camry'           AS model, 'PETROL'   AS fuel_type, 5 AS seating_capacity, 45.00  AS daily_rate, 'AVAILABLE'          AS status, 2023 AS year, 'CA-2023-001' AS license_plate, 'Reliable midsize sedan with smooth ride and great fuel economy.' AS description
    UNION ALL SELECT 'Honda',           'Civic',           'PETROL',   5, 42.00,  'AVAILABLE',          2022, 'CA-2022-002', 'Compact sedan ideal for city driving and daily commutes.'
    UNION ALL SELECT 'Tesla',           'Model 3',         'ELECTRIC', 5, 85.00,  'AVAILABLE',          2024, 'CA-2024-003', 'All-electric sedan with autopilot and fast charging support.'
    UNION ALL SELECT 'Ford',            'Mustang',         'PETROL',   4, 95.00,  'RENTED',             2021, 'CA-2021-004', 'Iconic American muscle car with powerful V8 performance.'
    UNION ALL SELECT 'BMW',             'X5',              'DIESEL',   7, 120.00, 'AVAILABLE',          2023, 'CA-2023-005', 'Luxury SUV with spacious interior and premium features.'
    UNION ALL SELECT 'Mercedes-Benz',   'C-Class',         'HYBRID',   5, 110.00, 'AVAILABLE',          2022, 'CA-2022-006', 'Elegant luxury sedan blending comfort with hybrid efficiency.'
    UNION ALL SELECT 'Audi',            'A4',              'PETROL',   5, 98.00,  'AVAILABLE',          2023, 'CA-2023-007', 'Sporty executive sedan with quattro all-wheel drive.'
    UNION ALL SELECT 'Hyundai',         'Tucson',          'HYBRID',   5, 55.00,  'AVAILABLE',          2022, 'CA-2022-008', 'Versatile compact SUV with modern tech and hybrid powertrain.'
    UNION ALL SELECT 'Kia',             'Sportage',        'PETROL',   5, 52.00,  'AVAILABLE',          2023, 'CA-2023-009', 'Stylish crossover with ample cargo space for family trips.'
    UNION ALL SELECT 'Nissan',          'Altima',          'PETROL',   5, 40.00,  'UNDER_MAINTENANCE',  2021, 'CA-2021-010', 'Comfortable sedan currently undergoing scheduled service.'
    UNION ALL SELECT 'Chevrolet',       'Tahoe',           'DIESEL',   8, 130.00, 'AVAILABLE',          2022, 'CA-2022-011', 'Full-size SUV perfect for large groups and long road trips.'
    UNION ALL SELECT 'Jeep',            'Wrangler',        'PETROL',   5, 75.00,  'AVAILABLE',          2023, 'CA-2023-012', 'Off-road capable 4x4 built for adventure and rugged terrain.'
    UNION ALL SELECT 'Mazda',           'CX-5',            'PETROL',   5, 58.00,  'AVAILABLE',          2022, 'CA-2022-013', 'Driver-focused crossover with premium interior finishes.'
    UNION ALL SELECT 'Subaru',          'Outback',         'HYBRID',   5, 62.00,  'AVAILABLE',          2023, 'CA-2023-014', 'All-wheel-drive wagon-SUV hybrid for all-weather confidence.'
    UNION ALL SELECT 'Volkswagen',      'Golf',            'PETROL',   5, 38.00,  'AVAILABLE',          2021, 'CA-2021-015', 'Compact hatchback with agile handling and practical layout.'
    UNION ALL SELECT 'Lexus',           'RX 350',          'HYBRID',   5, 115.00, 'AVAILABLE',          2024, 'CA-2024-016', 'Refined luxury crossover with whisper-quiet cabin.'
    UNION ALL SELECT 'Porsche',         '911 Carrera',     'PETROL',   4, 250.00, 'RENTED',             2022, 'CA-2022-017', 'Legendary sports car delivering thrilling performance.'
    UNION ALL SELECT 'Land Rover',      'Range Rover Evoque', 'DIESEL', 5, 145.00, 'AVAILABLE',          2023, 'CA-2023-018', 'Premium compact SUV with distinctive design and comfort.'
    UNION ALL SELECT 'Toyota',          'RAV4',            'HYBRID',   5, 65.00,  'AVAILABLE',          2023, 'CA-2023-019', 'Best-selling hybrid SUV with excellent reliability.'
    UNION ALL SELECT 'Honda',           'CR-V',            'PETROL',   5, 60.00,  'AVAILABLE',          2022, 'CA-2022-020', 'Family-friendly SUV with generous rear legroom.'
    UNION ALL SELECT 'Ford',            'F-150',           'PETROL',   5, 90.00,  'AVAILABLE',          2023, 'CA-2023-021', 'America''s best-selling pickup with towing capability.'
    UNION ALL SELECT 'Tesla',           'Model Y',         'ELECTRIC', 7, 95.00,  'AVAILABLE',          2024, 'CA-2024-022', 'Electric crossover with panoramic glass roof and 7 seats.'
    UNION ALL SELECT 'BMW',             '3 Series',        'HYBRID',   5, 88.00,  'AVAILABLE',          2022, 'CA-2022-023', 'Dynamic sports sedan with hybrid efficiency.'
    UNION ALL SELECT 'Audi',            'Q7',              'DIESEL',   7, 135.00, 'UNDER_MAINTENANCE',  2023, 'CA-2023-024', 'Three-row luxury SUV temporarily unavailable for maintenance.'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM cars LIMIT 1);

INSERT INTO car_images (car_id, storage_path, url, sort_order)
SELECT c.id, CONCAT('external:', img.url), img.url, img.sort_order
FROM (
    SELECT 'CA-2023-001' AS license_plate, 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800' AS url, 0 AS sort_order
    UNION ALL SELECT 'CA-2023-001', 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800', 1
    UNION ALL SELECT 'CA-2022-002', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', 0
    UNION ALL SELECT 'CA-2024-003', 'https://images.unsplash.com/photo-1619767886558-efdc259cde1a?w=800', 0
    UNION ALL SELECT 'CA-2024-003', 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800', 1
    UNION ALL SELECT 'CA-2021-004', 'https://images.unsplash.com/photo-1584345604476-8eff5e6e6a0d?w=800', 0
    UNION ALL SELECT 'CA-2023-005', 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=800', 0
    UNION ALL SELECT 'CA-2022-006', 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800', 0
    UNION ALL SELECT 'CA-2023-007', 'https://images.unsplash.com/photo-1606157922522-1cfa7671a1b2?w=800', 0
    UNION ALL SELECT 'CA-2022-008', 'https://images.unsplash.com/photo-1609521263047-f8f205293bb4?w=800', 0
    UNION ALL SELECT 'CA-2023-009', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', 0
    UNION ALL SELECT 'CA-2021-010', 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=800', 0
    UNION ALL SELECT 'CA-2022-011', 'https://images.unsplash.com/photo-1533473357861-0d5f0e3d16ed?w=800', 0
    UNION ALL SELECT 'CA-2023-012', 'https://images.unsplash.com/photo-1533473357861-0d5f0e3d16ed?w=800', 0
    UNION ALL SELECT 'CA-2022-013', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', 0
    UNION ALL SELECT 'CA-2023-014', 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=800', 0
    UNION ALL SELECT 'CA-2021-015', 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800', 0
    UNION ALL SELECT 'CA-2024-016', 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800', 0
    UNION ALL SELECT 'CA-2022-017', 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800', 0
    UNION ALL SELECT 'CA-2022-017', 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800', 1
    UNION ALL SELECT 'CA-2023-018', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', 0
    UNION ALL SELECT 'CA-2023-019', 'https://images.unsplash.com/photo-1609521263047-f8f205293bb4?w=800', 0
    UNION ALL SELECT 'CA-2022-020', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', 0
    UNION ALL SELECT 'CA-2023-021', 'https://images.unsplash.com/photo-1533473357861-0d5f0e3d16ed?w=800', 0
    UNION ALL SELECT 'CA-2024-022', 'https://images.unsplash.com/photo-1619767886558-efdc259cde1a?w=800', 0
    UNION ALL SELECT 'CA-2022-023', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800', 0
    UNION ALL SELECT 'CA-2023-024', 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=800', 0
) AS img
JOIN cars c ON c.license_plate = img.license_plate
WHERE NOT EXISTS (SELECT 1 FROM car_images LIMIT 1);
