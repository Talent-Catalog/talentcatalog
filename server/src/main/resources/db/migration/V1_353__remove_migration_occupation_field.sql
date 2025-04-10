-- Query to find exact and near-exact matches
SELECT
    co.migration_occupation,
    o.name,
    o.id,
    CASE
        WHEN TRIM(LOWER(co.migration_occupation)) = TRIM(LOWER(o.name)) THEN 'Exact Match'
        WHEN TRIM(LOWER(co.migration_occupation)) LIKE TRIM(LOWER(o.name)) || '%'
            OR TRIM(LOWER(o.name)) LIKE TRIM(LOWER(co.migration_occupation)) || '%' THEN 'Near-Exact Match'
        ELSE 'No Match'
        END AS match_type
FROM
    candidate_occupation co
        LEFT JOIN
    occupation o
    ON
        TRIM(LOWER(co.migration_occupation)) = TRIM(LOWER(o.name))
            OR TRIM(LOWER(co.migration_occupation)) LIKE TRIM(LOWER(o.name)) || '%'
            OR TRIM(LOWER(o.name)) LIKE TRIM(LOWER(co.migration_occupation)) || '%'
WHERE
    co.occupation_id = 0
  AND co.migration_occupation IS NOT NULL
ORDER BY
    co.migration_occupation, match_type;

UPDATE candidate_occupation co
SET occupation_id = o.id
FROM occupation o
WHERE TRIM(LOWER(co.migration_occupation)) = TRIM(LOWER(o.name))
  AND co.occupation_id = 0
  AND co.migration_occupation IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = o.id
);

-- Manual Mapping
-- First Correct 100
UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Decor and Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكور جبصين ودهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Board and Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكور جبسن بورد ودهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Maintenance and Installation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة وتركيب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Packaging Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشغل ماكينات تعبئة وتغليف ورولات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translation - closest match for translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);
UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Carpenter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منجور المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Key Turner)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خراطة مفاتيح'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10004 -- Stonemason (Stone and Tile Mason)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم عمار في الحجر الصخري الطبيعي والبلاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10004
);


UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Control - assuming vehicle-related control)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كنترول'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Saw Cutter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'قص على المنشار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Medical Educator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تثقيف طبي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8607 -- Child-care worker (Child Protection Monitor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'child protection monitor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8607
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Administrative Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف اداري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer - Arabic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Heating and Cooling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'heating and cooling'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Mechanical Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم ميكانيك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Plumbing and Electrical Tools)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادوات صحيه وكهربائي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Curtain Tailor and Upholsterer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خياط برادي ..البسة ..تنجيد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني طباعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 10004 -- Stonemason (Stone and Tile Mason)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معمار الحجر والبلاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10004
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Karting Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب سيارات كارتنغ'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Jewelry Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صياغة مجوهرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Western Food Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شف اكل غربي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Functional and Organizational Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم وظيفي وتنظيمي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);


UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Manual Laborer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'يد عاملة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Carpet Factory Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معمل سجاد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاق ( كوافير )'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Assistant Train Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assistant of train driver'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Personal Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'personal trainer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Hospital Admissions/Warehouse Clerk)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكتب دخول المرضى/ امين مستودع المستشفى'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Smartphone and Electronics Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'إصلاح الهاتف الذكي و اللوائح الإلكترونية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Curtain Tailor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خياط برادي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8492 -- Baker or pastry-cook (Baker and Assistant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فران ومساعد شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8492
);


UPDATE candidate_occupation co
SET occupation_id = 9441 -- Business Owner (Product Owner - Founder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'product owner - founder'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9441
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8607 -- Child-care worker (Psychosocial Support for Children)
WHERE TRIM(LOWER(co.migration_occupation)) = 'psychosocial support for children'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8607
);

UPDATE candidate_occupation co
SET occupation_id = 8685 -- Professor or Lecturer (Lecturer in Humanitarian Organizations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محاضر في منظمات انسانية ، مصور وعامل في مجال الإعلام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8685
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Stone Carver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحات حجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة كمبيوتر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Bellboy)
WHERE TRIM(LOWER(co.migration_occupation)) = 'bellboy'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teaching Assistance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'teaching assistance'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8591 -- Athlete or sportsperson (Football Player)
WHERE TRIM(LOWER(co.migration_occupation)) = 'لاعب كرة قدم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8591
);

UPDATE candidate_occupation co
SET occupation_id = 10014 -- Mechanic, diesel: motor vehicle (Taxi Mechanic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ميكانيكي سيارات تاكسي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10014
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma Restaurant Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطعم شاورما/ معلم/'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gold and Jewelry Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة الذهب والمجوهرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Receptionist, Call Center)
WHERE TRIM(LOWER(co.migration_occupation)) = 'receptionist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Receptionist, Call Center)
WHERE TRIM(LOWER(co.migration_occupation)) = 'receptionist, call center'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Grader Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشغل جريدر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Journalist - closest match)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صحفي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (False Ceilings and Thermal Insulation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'أسقف مستعارة + عازل حراري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8707 -- Upholsterer or related worker (Furniture Upholsterer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منجد مفروشات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8707
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Jewelry Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صانع مجوهرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8657 -- Lawyer (Legal Information Provider)
WHERE TRIM(LOWER(co.migration_occupation)) = 'legal informations'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8657
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Tourism and Travel Office Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكاتب السياحة والسفر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychological and Social Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'أخصائية نفسية واجتماعية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Research Writer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research writer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Pharmaceutical Marketing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'pharamceutical marketing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Cleaning Supplies Factory Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل مصنع أدوات تنظيف (صابون_شامبو _سائل جلي..الخ..)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Non-Destructive Testing Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'non destructive testing using gama ray'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Laboratory Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مخبرية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher - general)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرسه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 10000 -- Archaeologist (Archaeological Excavator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'التنقيب عن الآثار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10000
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Decorations and False Ceilings)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكورات جبسيه وأسقف مستعارة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Commercial Shop Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل في محل تجاري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Sculptor and Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحات ورسام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Plumbing Installer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تمديدات صحيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Fashion Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم الأزياء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Body Repairer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سمكرة سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (PVC and Decor Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم ديكور و pvc'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8527 -- Garbage collector (Cleaner)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل نظافة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8527
);

UPDATE candidate_occupation co
SET occupation_id = 8632 -- Filmmaker (Video Editor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'video editor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8632
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wall Painter and Interior Decorator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طلاء جدران (داخلي-خارجي) و ديكورات داخلية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project manger'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Research)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Hotelior - assuming hotel-related role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hotelior'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plastic Production and Operation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني انتاج وتشغيل بلاستيك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Coordinator Erection - managerial role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'coordinator erection'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Wash Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'wash worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Facilitator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'facilitator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (False Ceilings and Gypsum Board)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اسقف مستعارة وجبسم بورد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Assistant Department Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معاون مدير قسم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Inventory Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'inventory keeper'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Kitchen Factory)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصنع مطابخ ألمنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Traditional Jewelry Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة موجوهرة تقليدية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);



UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Computer Network)
WHERE TRIM(LOWER(co.migration_occupation)) = 'computer network'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Clinical Psychologist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'clinical psychologist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Facility Coordinator/Fleet Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'facility coordinator/fleet coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Household Electricity, AC, and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني كهرباء منزلية وتكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouse Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'أمين مستودع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plastic Hose Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع خراطيم بلاستيكيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (First Aid and Nursing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'first aid and nursing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing and School Notebook Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الطباعة وصناعة الدفاتر المدرسية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Painting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بويا سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Interpretation/Translation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'interpretation/ translation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8500 -- Butcher (Butcher/Welder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جزار لحام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8500
);

UPDATE candidate_occupation co
SET occupation_id = 8529 -- General manager in wholesale or retail (Supermarket)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سوبر ماركت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8529
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (Hematologist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hematologist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Control and Order Preparation for Pharmacy Warehouse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كنترول وتحضير طلبيات ادوية للصيدليات مستودع ادوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Heavy Equipment Driver: Grader, Agricultural Machines, Shipping)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق معدات ثقيلة كريدر وآلات زراعيةوشحن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);


UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Fast Food Restaurant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف مطعم وجبات سريعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hair Styling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصفيف الشعر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Programmer at Spacetoon Station)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معدة برامج في محطة سبيستون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Interpreter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'interpreter'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8653 -- Humanitarian worker (NGO - assuming humanitarian role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ngo'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8653
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Receptionist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'recepetionist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Radiographer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'radiographer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Rock and Marble Sculptor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحت صخر ورخام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Research Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Laser Cut Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lasercut machine operator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الموبيلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decor Worker: Ceilings, Gypsum Board, and Electrician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل ديكور اسقف وجبس بورد وعامل كهرباء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Supply Chain - imports/exports)
WHERE TRIM(LOWER(co.migration_occupation)) = 'supply chain ( imports/exports)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Laboratory Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'laboratory technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tinsmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سنكري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Graduate Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'graduate assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Commis - kitchen assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كومي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'maintenance'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Vocation - vocational trade)
WHERE TRIM(LOWER(co.migration_occupation)) = 'vocation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (University Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب جامعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Public Transport Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق نقل عام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Stock Controller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'stock controller'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 10016 -- Scientist, data mining (Junior Data Scientist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'junior data scientist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10016
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Community Health Educator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'community health educator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Assistant Professor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد استاذ'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8531 -- Healthcare professional (hospice) (Home Visit Team)
WHERE TRIM(LOWER(co.migration_occupation)) = 'home visit team'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8531
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Outreach Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'outreach worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Metal Turning and Leveling Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني خراطة وتسوية معادن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Lighting Technician for Events)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اضاءة حفلات واعراس ومهرجانات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum, Wooden Kitchens, and Glass Work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'المهنة المنيوم ومطابخ خشبية والمنيوم وزجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Laboratory Technicians)
WHERE TRIM(LOWER(co.migration_occupation)) = 'laboratory technicians'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Control Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'conrol operator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ممرض'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Mechanical Project Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'mechanical project engineer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Researcher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'باحث'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Manufacturing for Homes and Buildings)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع المنيوم للمنازل والابنية والشركات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (DJ)
WHERE TRIM(LOWER(co.migration_occupation)) = 'dj'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8628 -- Engineer (electrical) (Assistant Licensed in Electrical Engineering)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد مجاز في الهندسة الكهربائية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8628
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (House Paint Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'house paint teacher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Case Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'case manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (3rd Year Biomedical Science Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالبة سنه الثالثة في العلوم المخبرية biomedical science'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Case Manager/Data Verifier)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير حاله (مدقق بيانات)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Food Store Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'food store supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'printing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 9441 -- Business Owner (Own a Mobile Shop in Syria)
WHERE TRIM(LOWER(co.migration_occupation)) = 'own a mobile shop in syria'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9441
);

UPDATE candidate_occupation co
SET occupation_id = 8643 -- Graphic designer (Graphic Artist - 2D Animator and Illustrator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'graphic artist - 2d animator and illustrator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8643
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Glass Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب زجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Bodybuilding, Powerlifting, and Fitness Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'bodybuilding, powerlifting, and fitness trainer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكيف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8680 -- Photographer, image or sound equipment operator (Communications Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8680
);

UPDATE candidate_occupation co
SET occupation_id = 8696 -- Ship and aircraft controller or technician (Sea Captain)
WHERE TRIM(LOWER(co.migration_occupation)) = 'قبطان بحري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8696
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف خدمة عملاء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Production Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول انتاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Travel Agency Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف غي مكتب سفريات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Administrative Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير اداري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plastering and Internal/External Insulation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعمال الطرش والعزل الخارجي والداخلي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8632 -- Filmmaker (YouTuber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'you tuber'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8632
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Store Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'store manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Repair Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني وفني تصلح تلفونات هواتف محمولة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Repair Broken Smartphones)
WHERE TRIM(LOWER(co.migration_occupation)) = 'repair broken smartphones'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Radiology Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني أشعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Barber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'iam a barber'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);


UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Technical Support)
WHERE TRIM(LOWER(co.migration_occupation)) = 'technical support'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Sales and Marketing Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'sales and marketing manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Sales and Marketing Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'sales'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Psychosocial Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'psychosocial officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Internship)
WHERE TRIM(LOWER(co.migration_occupation)) = 'internship'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (intern)
WHERE TRIM(LOWER(co.migration_occupation)) = 'intern'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة جوالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator - Arabic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مترجم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Building Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان مباني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8500 -- Butcher (Butcher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'قصاب جزار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8500
);


UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Coach)
WHERE TRIM(LOWER(co.migration_occupation)) = 'coach'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (Recently Graduated General Doctor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طبيب بشري عام متخرج حديثا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Technician - Arabic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Interior/Exterior Painter and Leak Prevention)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان داخلي و خارجي و منع النش'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing and Advertising)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طباعة واعلان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Paint Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'paint worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Drask - assuming student-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دراسك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);


UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Trainer, Kids Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'trainer, kids teacher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طاهي او شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Deputy Warehouse Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نائب امين مستودع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Food Production Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير انتاج مواد غذائية وإدارة اقسام الانتاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Researcher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'researcher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Computer Science - Networking Technology)
WHERE TRIM(LOWER(co.migration_occupation)) = 'computer science - networking technology'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Elevator Installation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تركيب مصاعد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Storekeeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'storekeeper'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Handmade Artistic Accessories and Oriental Items)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعمال يدوية فنية اكسسوارات وشرقيات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Delivery and Assembly)
WHERE TRIM(LOWER(co.migration_occupation)) = 'delivery and assembly'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Hotel Management Diploma Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب دبلوم إدارة فنادق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Conditioning and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'conditioning and refrigeration technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8574 -- Waiter, waitress, bartender, restaurant worker (Head Waiter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'head waiter'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8574
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Sprayer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بخاخ سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Laboratory Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل مختبرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Goldsmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'gold smith'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8632 -- Filmmaker (Ad Montage)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مونتاج اعلانان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8632
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Key Maker and Smart Key Programming)
WHERE TRIM(LOWER(co.migration_occupation)) = 'key maker and smart key programing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Elevator Installation, Operation, Programming, and Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب و تشغيل و برمجة و صيانة مصاعد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Program Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'program coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10004 -- Stonemason (Tile Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بلاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10004
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Digital Printing Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني طباعة رقمية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

-- Third Correct:
UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Car Trading)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تجارة السيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Logistics Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'logistics supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (In Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'في صيانة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Taxi Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق سيارة اجرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture for Salons)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مفروشات صالونات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Fitness Sports Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب رياضي لياقة بدنية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8710 -- Welder or flamecutter (Electric Welder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'لحام كهرباء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8710
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoemaker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صانع احذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decoration Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ديكور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8529 -- General manager in wholesale or retail (Clothing Store Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير محل البسة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8529
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer and Network Maintenance and Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة وتركيب اجهزة الكمبيوتر و الشبكات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة اجهزة خلوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8498 -- Building construction labourer (Porter Building - assuming typo for construction-related role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'proter building'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8498
);

UPDATE candidate_occupation co
SET occupation_id = 8585 -- Archivist, curator, or librarian (Librarian)
WHERE TRIM(LOWER(co.migration_occupation)) = 'librarian'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8585
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Personal Fitness Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'personal fitness trainer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8602 -- Carpenter or joiner (Wooden Flooring Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب ارضيات خشب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8602
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Household Electricity, Welder, Car Tuning)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كهرباء منازل حداد لحام دوزان سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Admission & Registration Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'admission & registration officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Civil Status Department)
WHERE TRIM(LOWER(co.migration_occupation)) = 'civil status department'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Tailor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خياط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gemstones Installation Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'gemstones installation worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Heavy Truck Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'heavy truck driver'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing Press)
WHERE TRIM(LOWER(co.migration_occupation)) = 'printing press'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Roll Printing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طباعة رول'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Sewing, Textile, Embroidery, and Handicrafts)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خياطة واعمال نسيج وتطريز واعمال حرفية يدوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (General Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشرف عام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8653 -- Humanitarian worker (American Near East Refugee Aid)
WHERE TRIM(LOWER(co.migration_occupation)) = 'american near east refugee aid'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8653
);

UPDATE candidate_occupation co
SET occupation_id = 8500 -- Butcher (Butcher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'قصاب / جزار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8500
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Smartphone Maintenance and Programming)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الهواتف الذكيه وبرمجتها'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8619 -- Dentist (Dental Technology - assuming technician role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'dental technology'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8619
);

UPDATE candidate_occupation co
SET occupation_id = 10014 -- Mechanic, diesel: motor vehicle (Tire Repair, Oil Change, Car Wash)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصليح دواليب وغيار زيت ومغسل سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10014
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة الهواتف الخليوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم شاورما'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8680 -- Photographer, image or sound equipment operator (Communications Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني إتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8680
);

UPDATE candidate_occupation co
SET occupation_id = 8565 -- Sewer, embroiderer or related (Dry Cleaning and Ironing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصبغة للكي وتنظيف الملابس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8565
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Vegetable Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع خضار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Heavy Transport Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق شحن نقل ثقيل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Space Planning - Planogram Creator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'space planning (planogram creator)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Human-Computer Interaction)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human-computer interaction'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سكيوريتي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سكيورتي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Monitoring and Evaluation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'monitoring and evaluation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8496 -- Builder (traditional materials) (Capacity of Scaffolder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'capacity of scaffolder'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8496
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Blacksmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداد سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Heavy Truck Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق شاحنات ثقيله'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8565 -- Sewer, embroiderer or related (Steam Ironing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوي بخار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8565
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Commercial Services Manager + Stores Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'commercial services manager + stores manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile and Computer Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة هواتف محمولة وصيانة كمبيوتر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Board and Painting Decoration Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ديكور جبسن برد ودهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Refrigeration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8783 -- Engineer (petroleum) (Oil and Gas)
WHERE TRIM(LOWER(co.migration_occupation)) = 'oil and gas'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8783
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Laboratory Analysis Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تحليل مخبري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Cooler Factory Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصنع بردات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Sales Person)
WHERE TRIM(LOWER(co.migration_occupation)) = 'sales person'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Fashion Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم ازياء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Heating, Air Conditioning, and Refrigeration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدفئة و تكييف وتبريد الهواء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (I am learning)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اتعلم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plastic Injection Molding Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'plastic injection molding technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8694 -- Secretary (Secretary)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سكرتيره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8694
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Network Gaming Center)
WHERE TRIM(LOWER(co.migration_occupation)) = 'network gaming center'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Sales Representative in Company)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندوب المبيعات في الشركة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Car Seller, Private Driver, Delivery, Data Entry)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بايع سيارات...سايق خاص..توصيل...مدخل بيانات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Consultant Travel Agent)
WHERE TRIM(LOWER(co.migration_occupation)) = 'consultant travel agent'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Mobiles and Computers Engineering - Software and Hardware)
WHERE TRIM(LOWER(co.migration_occupation)) = 'mobiles and computers engineering.. (software and hardware)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان موبليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customer service'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Security Camera Installation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تركيب كاميرات مراقبة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Central Ventilation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'التهويه المركزيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8710 -- Welder or flamecutter (Welding)
WHERE TRIM(LOWER(co.migration_occupation)) = 'welding'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8710
);


UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioner Installation Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم في تركيب المكيفات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Sales Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'sales coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Sales Representative)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندوب مبيعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانه جوالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Sworn Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'sworn translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Curtain Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم ستائر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Cellular Device Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الاجهزة الخلوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Drinking Water Filter Repair Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اصلاح فلاتر مياه الشرب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Unknown (Other)
WHERE TRIM(LOWER(co.migration_occupation)) = 'غير ذلك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wallpaper and Plaster Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ورق حيطان مليس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8651 -- Healthcare professional (physical therapist) (Physical Therapist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'physical therabist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8651
);

UPDATE candidate_occupation co
SET occupation_id = 10004 -- Stonemason (Tile Polisher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جلي بلاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10004
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني طباعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Fiber Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني فايبر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Powder Coating Industrial Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعي بوهيات بودره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Supervisor of Interior Decoration Company Workers)
WHERE TRIM(LOWER(co.migration_occupation)) = 'supervisor  مشرف عمال شركة ديكورات داخلية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Supermarket Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع سبر ماركة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ألمونتال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Shipping)
WHERE TRIM(LOWER(co.migration_occupation)) = 'shipping'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Hotelior)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hotelior'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plastic Production and Operation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني انتاج وتشغيل بلاستيك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Coordinator Erection)
WHERE TRIM(LOWER(co.migration_occupation)) = 'coordinator erection'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Wash Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'wash worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Facilitator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'facilitator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (False Ceilings and Gypsum Board)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اسقف مستعارة وجبسم بورد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Assistant Department Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معاون مدير قسم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Inventory Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'inventory keeper'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Kitchen Factory)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصنع مطابخ ألمنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Traditional Jewelry Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة موجوهرة تقليدية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer at Association)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع لدى جمعية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Computer Network)
WHERE TRIM(LOWER(co.migration_occupation)) = 'computer network'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Clinical Psychologist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'clinical psychologist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Facility Coordinator/Fleet Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'facility coordinator/fleet coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Household Electricity, AC, and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني كهرباء منزلية وتكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plastic Hose Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع خراطيم بلاستيكيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (First Aid and Nursing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'first aid and nursing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Printing and School Notebook Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الطباعة وصناعة الدفاتر المدرسية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Interpretation/Translation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'interpretation/ translation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8500 -- Butcher (Butcher/Welder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جزار لحام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8500
);

UPDATE candidate_occupation co
SET occupation_id = 8529 -- General manager in wholesale or retail (Supermarket)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سوبر ماركت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8529
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (Hematologist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hematologist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Control and Order Preparation for Pharmacy Warehouse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كنترول وتحضير طلبيات ادوية للصيدليات مستودع ادوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Heavy Equipment Driver: Grader, Agricultural Machines, Shipping)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق معدات ثقيلة كريدر وآلات زراعيةوشحن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);


UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Fast Food Restaurant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف مطعم وجبات سريعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hair Styling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصفيف الشعر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Programmer at Spacetoon Station)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معدة برامج في محطة سبيستون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decoration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ديكورات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8653 -- Humanitarian worker (NGO)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ngo'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8653
);


UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Radiographer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'radiographer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Rock and Marble Sculptor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحت صخر ورخام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Research Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Laser Cut Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lasercut machine operator.'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

-- Fourth forth
UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Operations Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'operations manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Radio Anchor, TV Presenter, Voice-Over)
WHERE TRIM(LOWER(co.migration_occupation)) = 'radio-anchor , tv-presenter , voice-over'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Polishing Clothing Accessories and Water Taps)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تلميع واطيب اكسسوارات الألبسة وحنفيات المياه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (English Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'english translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Procurement Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'procurement manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Design and Wooden Flooring)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم الامنيوم وارضيات الخشب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Coordination)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تنسيق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Printing Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل مطبعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Venture Capitalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'venture capitalist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 8651 -- Healthcare professional (physical therapist) (Physiotherapist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'physiotherapist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8651
);

UPDATE candidate_occupation co
SET occupation_id = 8669 -- Real estate agent (Real Estate Agent)
WHERE TRIM(LOWER(co.migration_occupation)) = 'real estate agent'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8669
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Branch Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'branch manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Working in Investment and Internet Marketing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعمل في مجال الاستثمار و التسويق عبر الانترنيت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Health Educator at Save the Children)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مثقفة صحية ضمن منظمة save the children'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Production)
WHERE TRIM(LOWER(co.migration_occupation)) = 'production'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Research and Monitoring Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research and monitoring assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Executive Manager in Telecom Company)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول تنفيذي في شركة اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Graduate of Drawing Institute)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خريجة معهد رسم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة حاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Media Professional)
WHERE TRIM(LOWER(co.migration_occupation)) = 'إعلامية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);


UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Nuclear Physics Researcher, Ph.D.)
WHERE TRIM(LOWER(co.migration_occupation)) = 'nuclear physics reseacher, ph.d'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Bachelor’s in Archaeology)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اجازة في علم الآثار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Buildings and Construction Project Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مشاريع مباني و انشاءات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);



UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Cook and Hairdresser for Women)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طباخه وحلاقه للسيدات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);


UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (Paramedic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسعف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Metal Turning and Shaping)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خراطة وتشكيل معادن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ممرض'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Building Insulation Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'أخصائي عزل أبنية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8680 -- Photographer, image or sound equipment operator (Communications Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8680
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Contact Center Executive)
WHERE TRIM(LOWER(co.migration_occupation)) = 'contact center executive'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8685 -- Professional worker not elsewhere classified (Tour Guide)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مرشد سياحي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8685
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (University Lecturer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدريسي في الجامعه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Sales Administrator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اداري مبيعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8565 -- Sewer, embroiderer or related (Dry Cleaning and Ironing Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم مصبغة في كوي وتنظيف الملابس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8565
);

UPDATE candidate_occupation co
SET occupation_id = 8498 -- Building construction labourer (Construction Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل بناء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8498
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Technical Director - French)
WHERE TRIM(LOWER(co.migration_occupation)) = 'directeur technique'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8565 -- Sewer, embroiderer or related (Carpet Washing, Clothing Washing, and Ironing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'غسيل سجاد وغسيل وكوي الالبسه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8565
);

UPDATE candidate_occupation co
SET occupation_id = 8628 -- Engineer (electrical) (Engineering Inspection - RT/X-Ray)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فحص هندسي (rt (x_ray))'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8628
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Pastry Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صانع معجنات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8574 -- Waiter, waitress, bartender, restaurant worker (Restaurant Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل مطعم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8574
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Logistics Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'logistics coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customs)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الجمارك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Representative - typo assumed)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندزب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Marketer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسوق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Language Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرسة لغة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Hospitality)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hospitality'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Fixed Assets Operation Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'fixed assets operation manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Membership Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'membership officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser Specializing in Hair and Skin Care)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافيرا اختصاص تجمسل شعر وتنضيف بشرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Lecturer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lecturer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Restaurant Specialist - Grill Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اخصائي مطعم شيف مشاوي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Public Relations in Tourism and Travel Company, Eastern/Western Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'علاقات عامة بشركةسياحة وسفر وشيف طبخ شرقي وجبات غربي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (House Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان منازل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8492 -- Armed forces (Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ضابط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8492
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Science Laboratory Technician, Lab Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني مختبر علوم مسؤول مختبر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Saleswoman)
WHERE TRIM(LOWER(co.migration_occupation)) = 'saleswoman'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Crane Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق رافعة كانتري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Seller + Paints)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع +دهانات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);


UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Paper Printing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطبعه ورق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Media Channel Decoration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكور قنوات اعلامية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car and Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان سيارات ودهان موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Car Rental)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تأجير سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security and Safety Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'security and safety officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Lighting and Sound Installation for Events)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب اضاءة وصوت وفي الحفلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'في مجال الهواتف المحمولة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Food Science and Nutrition)
WHERE TRIM(LOWER(co.migration_occupation)) = 'food science and nutrition'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Hardware Networking Pre-Sales - Cisco Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hardware networking pre-sales (cisco specialist)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Playground Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'playground supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10014 -- Mechanic, diesel: motor vehicle (Car Electricity + Tire Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كهرباء سيارات+اصلاح اطارات السيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10014
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Computer Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب كمبيوتر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);


UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Sales and Experience in Seeds and Nuts)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مبيع وخبرة في البزورات والمكسرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 8527 -- Refuse collector or related labourer (Cleaning)
WHERE TRIM(LOWER(co.migration_occupation)) = 'cleaning'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8527
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Dairy and Cheese Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم البان واجبان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Accessories and Electricity)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اكسسوار وكهرباء السيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8577 -- Accountant (Accountant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محاسب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8577
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Technical Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مراقب فني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (University Student with Programming and Computer Experience)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب جامعي لدي خبرة في البرمجة والحاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (University Student with Programming and Computer Experience)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب جامعي لدي خبرة في البرمجة  والحاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8574 -- Waiter, waitress, bartender, restaurant worker (Dishwasher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'dishwasher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8574
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ألمنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Interior Design)
WHERE TRIM(LOWER(co.migration_occupation)) = 'interior design'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8694 -- Secretary (Secretariat)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سكرتارية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8694
);

UPDATE candidate_occupation co
SET occupation_id = 9441 -- Business Owner (My Business)
WHERE TRIM(LOWER(co.migration_occupation)) = 'my business'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9441
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Sewage Treatment and Water Purification Plant Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'العمل في مجال محطات تنقية ومعالجة مياة الصرف الصحي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Makeup Artist and Hairstylist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'makeup artist and hairstylist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Collection Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'collection manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تكييف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8628 -- Engineer (electrical) (Low Current Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'low current engineer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8628
);

UPDATE candidate_occupation co
SET occupation_id = 10020 -- Other Sales Workers (Gasoline Sales, Car Wash, and Oil Change)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بيع بانزين وغسيل سيارات وغيار زيت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10020
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Assistant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Partition Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل مقسم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Visual Artist, Calligrapher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فنان تشكيلي، خطّاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8628 -- Engineer (electrical) (Assistant Industrial Electrical Engineer and Machine Programmer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد مهندس كهرباء صناعية و برمجة آلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8628
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Purchasing Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'purchasing assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);


UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Reception)
WHERE TRIM(LOWER(co.migration_occupation)) = 'إستقبال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator, Copywriter & Content Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translator, copywriter & content manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Web Designing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'web designing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Fashion Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصممة أزياء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Hotel Front Desk)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hotel front desk'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Second Year Mechanical Engineering)
WHERE TRIM(LOWER(co.migration_occupation)) = 'second year-mecanical engineering'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Refrigeration and Air Conditioning Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تبريد و تكييف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8627 -- Engineer (civil) (Assistant Civil Engineer - Surveyor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد مهندس مدني_ مساح'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8627
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Electronics and Communications Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني الكترونيات اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (House Painting Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم دهان منازل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Studying)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادرس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Engineer (building) (Architecture of Asylum)
WHERE TRIM(LOWER(co.migration_occupation)) = 'architecture of asylum'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Sprayer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بخاخ موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Hardware Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'maintenance of hardware'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service Representative and Back-Office Team Member)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customer service representative and back-office team member'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 10004 -- Stonemason (Marble Wall and Floor Polishing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جلي وتلميع رخام جدران وارضيات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10004
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Hotel Work, Assistant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فندقية،مساعدة شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Door and Window Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صانع ابواب ونوافذ المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Protection Project Coordinator & Senior Protection Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'protection project coordinator& sr.protection officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Women’s Shoe Tailor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خياط جزدين نسائية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Card Making for Events)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تشكيل كروت للمناسبات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8498 -- Building construction labourer (Builder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بناء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8498
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance Expert)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خبير صيانة موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Information Technology)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكنولوجيا المعلومات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Radiology Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني تصوير شعاعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tinsmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سنكري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Sales and Marketing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'sales and marketing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Car Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق السيارة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Computer Professor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'استاذ حاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8680 -- Photographer, image or sound equipment operator (Communications Technician - Cable Welding)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اتصالات لحام كابلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8680
);

UPDATE candidate_occupation co
SET occupation_id = 8574 -- Waiter, waitress, bartender, restaurant worker (Customer Service in Restaurants)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خدمة زبائن في المطاعم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8574
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (CNC Lathe Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشغل ماكينة خراطة cnc'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Intermediate Institute for Archaeology and Museums/Excavation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معهد متوسط للآثار والمتاحف/تنقيب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Calibration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'calibration technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Auto Paint)
WHERE TRIM(LOWER(co.migration_occupation)) = 'auto paint'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Outreach Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'outreach worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8783 -- Engineer (petroleum) (Gas Process and Operations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'gas process and operations'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8783
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Maintenance of Mobile Phones)
WHERE TRIM(LOWER(co.migration_occupation)) = 'maintenance of mobile phones'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Electronics and Electronic Weighing Systems Maintenance Expert)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خبرة بصيانة الألكترونيات وأنظمة الوزن الألكتروني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);
UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Cars Trading)
WHERE TRIM(LOWER(co.migration_occupation)) = 'cars trading'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Monitoring and Evaluation Department Manager in Humanitarian Organizations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير قسم مراقبة وتقييم في منظمات انسانية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser - Women’s Beauty)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافير .. تجميل سيدات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);


UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Hatchery Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hatchery worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8527 -- Refuse collector or related labourer (Cleaner and Maintenance Worker at CIS Institutes)
WHERE TRIM(LOWER(co.migration_occupation)) = 'أعمل في مجموعة معاهدcis  عامل تنضيف وصيانة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8527
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Property caretaker (House or Institution Guard and Gardener)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ناطور منزل او موسسة وجنيناتي (اهتم بالحديقة)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Barber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8627 -- Engineer (civil) (Quantity Surveyor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'quantity surveyor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8627
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Database Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'database officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Interpreter/Cultural Mediator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'interpreter/ cultural mediator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);


UPDATE candidate_occupation co
SET occupation_id = 9436 -- Stay-at-home spouse (Housewife)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ربت منزل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9436
);


UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (High School Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب مدرسة ثانوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'air conditioning technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Photocopy Machine Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hotocopy machine technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser - Women and Men)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافير حلاق نسائي رجالي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Painter and Graphic Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'رسام ومصمم غرافك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Dental Laboratory Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مخبري أسنان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Optics Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اخصائي بصريات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Quality Inspector)
WHERE TRIM(LOWER(co.migration_occupation)) = 'quality inspector'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Phone Maintenance Expert)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خبير صاينه تلفونات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Early Childhood Facilitator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ميسر طفولة مبكرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الإمنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Fashion Design)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم ازياء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator - Female)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مترجمة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8574 -- Waiter, waitress, bartender, restaurant worker (Bartender)
WHERE TRIM(LOWER(co.migration_occupation)) = 'bartender'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8574
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance Technician - Software and Hardware)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيلنة أجهزة هواتف متحركة سفت وير وهارد وير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service Trainee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customer service trainee'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

-- Fifth

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma Chef - Chicken and Meat with Experience)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف شاورما دجاج ولحمة بخبرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Express Mail and Cargo)
WHERE TRIM(LOWER(co.migration_occupation)) = 'express mail and cargo'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Karting Car Trainer and Small Truck Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب سيارات كارتنغ وسائق شاحنة صغيرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Logistics)
WHERE TRIM(LOWER(co.migration_occupation)) = 'logistics'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Journalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صحفية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouse Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول مستودعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Research Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research assistance'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Branch Manager of Small Finance Bank)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير فرع بنك تمويل صغير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Metal Offset Printing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'metal offset printing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Psychological Counselor/Psychology)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مرشدة نفسيه/علم نفس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8783 -- Engineer (petroleum) (Oil Equipment Technician in Drilling and Exploration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني معدات نفطية في مجال الحفر والتنقيب عن النفط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8783
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Administrative Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير إداري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Bamboo Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صنع الخيزران'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customer service'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Back Office Executive)
WHERE TRIM(LOWER(co.migration_occupation)) = 'back office executive'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Taekwondo Center Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'taekwondo center managemet'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Laboratory Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'laboratory technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Quality Control Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'quality control officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (Registered Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'registered nurse'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Juice, Cocktail, and Fruit Salad Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف عصائر وكوكتيلات وسلطات فواكة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Onsite Facilitator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'onsite facilitator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);


UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteering at Syrian Trust for Development)
WHERE TRIM(LOWER(co.migration_occupation)) = 'التطوع في الأمانة السورية للتنمية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (telecommunications) (Communications Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Psychology - Psychological Counseling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'علم النفس (الإرشاد النفسي)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Supervisor at Syrian Virtual University)
WHERE TRIM(LOWER(co.migration_occupation)) = 'supervisor at syrian virtual university'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Fulfillment Associate)
WHERE TRIM(LOWER(co.migration_occupation)) = 'fulfillment associated'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);


UPDATE candidate_occupation co
SET occupation_id = 8498 -- Building construction labourer (Artesian Well Driller for Drinking Water)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حفار ابار ارتوازية لمياه الشرب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8498
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Director/CEO Non-Profit Sector)
WHERE TRIM(LOWER(co.migration_occupation)) = 'director / ceo non-profit sector'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'nurse'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Clinical Psychologist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'clinical psychologist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (PR Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'pr coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (NGOs)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ngos'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum and Kitchens)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الالمنيوم و المطابخ'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Radio Announcer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'radio announcer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);


UPDATE candidate_occupation co
SET occupation_id = 10014 -- Mechanic, diesel: motor vehicle (General Mechanical Technician - Lathe and Automation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ميكانيك عام خراطه و اسويه آليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10014
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Graphic Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'graphic designer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (IT and Network Administrator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'it and network administrator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Engineer (building) (Architectural and Decor Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس معماري وديكور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Technical Warehouse Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'technical warehouse keeper'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Glass Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'زجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources Intern)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources intern'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Executive Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'executive manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Elevator and Escalator Installation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تركيب مصاعد وسلالم متحركة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (WordPress Website Design and Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم مواقع وردبرس وادارتها'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Corel Designer and Seals Responsible)
WHERE TRIM(LOWER(co.migration_occupation)) = 'corel( computer program ) designer and seals responsible'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8680 -- Photographer, image or sound equipment operator (Internet Network Connection Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني توصيل شبكات انترنت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8680
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Glass Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب زجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Reviewer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'reviewer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (News Editor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'news editor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Sales and Reservations Manager with Accounting in a Tourism Company)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير الميعات والحجوزات (مع الحسابات)في شركة سياحية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Commercial)
WHERE TRIM(LOWER(co.migration_occupation)) = 'commercial'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tinsmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سنكري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouse Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'امين مستودعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8574 -- Waiter, waitress, bartender, restaurant worker (Coffee Shop Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل في كوفي شوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8574
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Petroleum Pipe Fabrication and Welding)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فبركة ولحام انابيب البترول'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);


UPDATE candidate_occupation co
SET occupation_id = 8680 -- Photographer, image or sound equipment operator (Internet Network Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني شبكات انترنت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8680
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Restaurant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطعم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile and Computer Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانه موبيلات وكمبيوترات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8577 -- Accountant (Accountant and HR Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محاسب ومدير شؤون موظفين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8577
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Legal Inspector at Sudan Public Prosecution)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مفتش قانوني لدي النيابه العامه بالسودان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer for Psychological and Community Support)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع للدعم النفسي والمجتمعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Home Refrigeration and Air Conditioning Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تبريد وتكييف منزلي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10014 -- Mechanic, diesel: motor vehicle (Mechanic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ميكانيكي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10014
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning Mechanic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكانيكي تكييف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Classroom Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرسةصف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);


UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Driver - typo assumed)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Digital Printing Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مجال الطباعة الديجيتال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Risk Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'risk officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مجال التكييف والتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decoration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ديكور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Media)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعلام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Employee in a Clothing Company)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف في شركه البسه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8565 -- Sewer, embroiderer or related (Clothing Ironing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوى ملابس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8565
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Tire Trading and Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تجارة إطارات وتصليحها'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 9436 -- Stay-at-home spouse (Housewife)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ربة منزل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9436
);

UPDATE candidate_occupation co
SET occupation_id = 8497 -- Agricultural labourer (Agriculture)
WHERE TRIM(LOWER(co.migration_occupation)) = 'زراعه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8497
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (IT)
WHERE TRIM(LOWER(co.migration_occupation)) = 'it'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Purchasing Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول مشتريات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Business Letters)
WHERE TRIM(LOWER(co.migration_occupation)) = 'business letters'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'mobile phone repair'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8627 -- Engineer (civil) (Assistant Civil Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد مهندس مدني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8627
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Instrument & Control Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'instrument & control supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Healthcare professional (nurse) (Healthcare - Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الرعاية الصحية ( ممرض'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Transportation and General Trade)
WHERE TRIM(LOWER(co.migration_occupation)) = 'النقل والتجارة العامة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة موبايلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Refrigeration and Air Conditioning Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تبريد وتكييف صيانه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tinsmith and Construction)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سنكري و بالعمارة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8628 -- Engineer (electrical) (Sound Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس صوت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8628
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Freelance Journalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صحفية مستقلة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);



UPDATE candidate_occupation co
SET occupation_id = 8497 -- Agricultural labourer (Farmer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مزارع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8497
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Heavy Machinery Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق اليات ثقيلة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Dental Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'dental technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Electronics Device Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة أجهزة الكترونية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);


UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Detergent and Fragrance Material Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع مواد المنظفات والمعطرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Sports Trainer - Crochet Handicrafts)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدربة رياضة .اشغال يدوية كروشيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Incomplete Law Studies)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دراسة حقوق غير منتهي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Crane Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشغل رافعه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Phones and Computers)
WHERE TRIM(LOWER(co.migration_occupation)) = 'هواتف و كومبيوتر (حاسوب)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Phones and Computers)
WHERE TRIM(LOWER(co.migration_occupation)) = 'هواتف و كومبيوتر (حاسوب)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Arranger)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Warehouse and Logistics Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مخزن وخدمات لوجستية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Archaeology and Museums Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب اثار ومتاحف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Interior Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'interior designer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Stone Sculptor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحات حجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8498 -- Building construction labourer (Buildings Technician and Inspector)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ومراقب ابنية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8498
);

UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Software Developer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'software developer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Natural and Artificial Flower Arranger)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق ازهار طبيعيه وصناعيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Car Scanner and Electrical Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سكانر وكهرباء سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteering)
WHERE TRIM(LOWER(co.migration_occupation)) = 'volunteering'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Carpentry Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم منجور الألمنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Logistics In Charge)
WHERE TRIM(LOWER(co.migration_occupation)) = 'logistic in charge'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (9 Years Experience in Flight Support Companies)
WHERE TRIM(LOWER(co.migration_occupation)) = '9 years experience in flight support companies'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (Political Science - assumed as a field of study)
WHERE TRIM(LOWER(co.migration_occupation)) = 'political science'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translation & Interpretation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translation & interpretation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8565 -- Sewer, embroiderer or related (Women’s Arts - Sewing - Embroidery)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فنون نسوية _ خياطة _ تطريز'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8565
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (GIS)
WHERE TRIM(LOWER(co.migration_occupation)) = 'gis'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service and Call Center)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customer service and call center'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Programs Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'programs coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Cable Factory Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مصنع كابلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 9436 -- Housewife (Housewife)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ربة منزل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9436
);

UPDATE candidate_occupation co
SET occupation_id = 8679 -- Photographer (Photographer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصور فوتغرافي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8679
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Services Agent)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customer services agent'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

-- Sixth

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Medical and Dental Prosthetic Technicians (Professional Dental Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني أسنان محترف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);

UPDATE candidate_occupation co
SET occupation_id = 8521 -- Finance professional (Banking)
WHERE TRIM(LOWER(co.migration_occupation)) = 'banking'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8521
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Truck Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق شاحنه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'volunteer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Senior Web Developer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'senior web developer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8710 -- Welder or flamecutter (Stainless Welder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'stainless welder'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8710
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Installation and Maintenance of Surveillance Cameras)
WHERE TRIM(LOWER(co.migration_occupation)) = 'installation and maintenance of surveillance cameras'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);


UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (English Arabic Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'english arabic translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Food Packing and Wrapping Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل تعبئة وتفليف. مواد غذائية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8781 -- Engineer (electronic) (Intelligent Building Industry and Automation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'intelligent building industry and automation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8781
);

UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Web Developer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'web developer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس مشروع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Communications Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل في الاتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);


UPDATE candidate_occupation co
SET occupation_id = 8685 -- Professor or Lecturer (Lecturer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lecturer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8685
);

UPDATE candidate_occupation co
SET occupation_id = 8783 -- Engineer (petroleum) (Wire-line and Petroleum Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'wire-line and petroleum technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8783
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Journalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'journalist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Seller in a Telecom Shop)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع في محل للاتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Traffic Officer Department of Baggage Services)
WHERE TRIM(LOWER(co.migration_occupation)) = 'traffic officer department of baggage services'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Fabric Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع اقمشة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم احذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Home Electrical Appliance Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة اجهزة كهربائية منزلية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Singer Specializing in Eastern Music)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مغنيه اختصاص شرقي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8632 -- Filmmaker (Cinematography)
WHERE TRIM(LOWER(co.migration_occupation)) = 'cinematography'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8632
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer in Education)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع في مجال التعليم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer in Multiple Organizations and Agricultural Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع باكثرمن منظمة ومهندس زراعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Field Volunteer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تطوع ميداني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Maintenance and Programming Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانه وبرمجة الحاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Elevator Installation and Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب وإصلاح المصاعد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف شاورما'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Flight Reservations in Tourism Offices)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حجوزات طيران في مكاتب سياحية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Cultural Center Manager and French Teacher for High School Students)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مديرة مركز ثقافي ومدرسة فرنسي فندقي لطلاب الثانوي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم شاورما'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Communications Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Mosaic Artist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فنان فسيفساء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8593 -- Biologist (Biological Analyst)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محلل بايولوجي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8593
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Sculptor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (News Editor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محررة أخبار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8706 -- Cashier or ticket clerk (Cashier)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كاشير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8706
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Fashion Designer, Illustrator, and Comic Artist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم ازياء ورسام ورسام قصص مصورة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Human Development Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب تنمية بشرية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Installation of All Types of Modern Elevators)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب جميع انواع المصاعد الحديثة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Cheese and Dairy Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع البان و اجبان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);


UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Assistant Mechanical Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد مهندس مجاز في الهندسة الميكانيكية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouse Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشرف مستودعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Marble and Granite Decoration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ديكور رخام وجرانيت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma and Snack Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم شورما وسناك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Trainer - Arabic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرّب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Administrative)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادارية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8545 -- Metal worker (Specialist in Manufacturing and Executing Metal Structures)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متخصص في أعمال تصنيع وتنفيذ المنشآت المعدنية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8545
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Solar Energy Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب طاقه شمسيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Medical and Dental Prosthetic Technicians (Dental Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'dental technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Elevator Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة المصاعد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8493 -- Blacksmith, tool maker, or forge worker (Blacksmith for Doors, Protection, and Railings)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداد فرنجي تصنيع ابواب وحمايه ودرابزين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8493
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance and Programming)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانه وبرمجه اجهزه موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Painting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'المهنة بويا السيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Warehouse Manager for Spare Parts Department)
WHERE TRIM(LOWER(co.migration_occupation)) = 'warehouse manager for the spareparts department'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 9436 -- Housewife (Housewife)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ربة منزل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9436
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (English Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'english teacher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);


UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);


UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Leasing Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'leasing manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Bodybuilding Coach)
WHERE TRIM(LOWER(co.migration_occupation)) = 'coach bodybuilding'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 10003 -- Dental Assistants and Therapists (Dentist Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعدة طبيب أسنان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10003
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Media Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'media officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Business Management, Supervision, and Monitoring)
WHERE TRIM(LOWER(co.migration_occupation)) = 'business management, supervision and monitoring'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Water Plumbing - assumed from "تكرار مياه")
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكرار مياه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8696 -- Ship and aircraft controller or technician (Chief Mate)
WHERE TRIM(LOWER(co.migration_occupation)) = 'chief mate'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8696
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Graffiti Artist and Character Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'graffiti artist and character designer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8521 -- Finance professional (Corporate Finance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'corporate finance'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8521
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Field Volunteer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع ميداني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Stationery and Ink Purchaser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشتريات القرطاسية والاحبار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلمة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8512 -- Data entry operator (Data Entry)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ندخلة بيانات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8512
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdressing, Beauty, and Hair Styling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاقة وتجميل وتصفيف الشعر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);


UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'security officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Research Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Medical Officer/Marketing Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'medical officer/marketing coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Tour Guide)
WHERE TRIM(LOWER(co.migration_occupation)) = 'tour guide'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Bus Coach)
WHERE TRIM(LOWER(co.migration_occupation)) = 'bus coach'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security)
WHERE TRIM(LOWER(co.migration_occupation)) = 'security'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Artist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'artist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (IT Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'it officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Community Engagement Advisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'community engagement advisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8782 -- Engineer (industrial) (Industrial Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'industrial management'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8782
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Truck Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'truck driver'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Taxi Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'taxi driver'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8643 -- Graphic designer (Artist and Graphic Designer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'artist and graphic designer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8643
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Route Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'route specialist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Cargo Sector Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير قطاع شحن بضائع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8656 -- Judge (Assistant Judge)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assistant judge'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8656
);


UPDATE candidate_occupation co
SET occupation_id = 8664 -- Marketing professional (Public Relationships)
WHERE TRIM(LOWER(co.migration_occupation)) = 'public relationships'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8664
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tailoring and Installation of Kitchens, Aluminum, and Glass Works)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تفصيل وتركيب مطابخ واعمال المنيوم وزجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Educational Psychology)
WHERE TRIM(LOWER(co.migration_occupation)) = 'educational psychology'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Governance Consultant - Training Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مستشار حوكمة - اخصائي تدريب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8649 -- Optometrist (Optics Specialist - Medical and Sunglasses, Contact Lenses with Eye Testing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اختصاصي بصريات ..نظارات طبية وشمسية وعدسات لاصقة مع فحص نظر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8649
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Educational Community Liaison Monitor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'educational community liaison monitor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (Ordinary Worker - too vague)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل عادي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Travel Consultant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'travel consultant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (بليط - unclear, possibly a typo)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بليط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoes - assumed as shoemaker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'احذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Lifeguard, Coach)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lifeguard,coach'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8624 -- Education methods specialist (Practical Education Subject Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'practical education subject supervisor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8624
);

UPDATE candidate_occupation co
SET occupation_id = 8521 -- Finance professional (Specialist in Microfinance/Operations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مختص بالتمويل الصغير/عمليات /'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8521
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Medical Translation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'medical translation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8617 -- Decorator and commercial designer (Assistant Decoration Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assistant decoration engineer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8617
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Factory Producer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'factory producer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 10016 -- Scientist, data mining (Data Analyst - Marketing Department)
WHERE TRIM(LOWER(co.migration_occupation)) = 'data analyst - marketing department'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10016
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Hospital Management and Medical Records)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادارة مستشفيات وسجل طبي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Writer, Law Graduate, and Human Rights Activist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كاتب وخريج قانون وناشط حقوقي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Consultant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'consultant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (House Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان منازل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

-- Seventh
UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة موبايلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Painting and Spraying)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان  وبخ  سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Human Rights Organization)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human rights organization'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Anesthesia and Resuscitation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تخدير وانعاش'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator, Interpreter, Proofreader)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translator, interpreter, proofreader'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Lifeguard)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lifeguard'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8607 -- Child-care worker (Children’s Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشرف اطفال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8607
);

UPDATE candidate_occupation co
SET occupation_id = 8613 -- Corporate director or chief executive (Founder and Director)
WHERE TRIM(LOWER(co.migration_occupation)) = 'founder and director'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8613
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouse - assumed as warehouse worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مستودع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Household Electricity)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كهرباء منازل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Journalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'journalist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychological)
WHERE TRIM(LOWER(co.migration_occupation)) = 'psychological'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8577 -- Accountant (Accounting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'المحاسبه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8577
);

UPDATE candidate_occupation co
SET occupation_id = 8577 -- Accountant (Accounting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محاسبه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8577
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Conditioning and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مٌدِرَسِ'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychologist and Mental Health Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اخصائي  نفسي وصحة نفسية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security Companies)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الشركات الامنيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8689 -- Safety, health & quality inspector (Safety Officer HSE)
WHERE TRIM(LOWER(co.migration_occupation)) = 'safety officer hse'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8689
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Men’s Hairdressing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاقه رجاليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (Multitalented - too vague)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متعدد المواهب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Personal Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'personal assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Printing - assumed as a craft)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الطباعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Administrative Worker for an Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل اداري لدى مهندس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Mechanical Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ميكانيكي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Central Heating)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدفئة مركزية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);


UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teaching)
WHERE TRIM(LOWER(co.migration_occupation)) = 'teaching'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8545 -- Metal worker (Metal Lathe Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خراطة معادن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8545
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Computer Programmer Hardware + Software / Smartphone Programmer Software)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مبرمج حواسيب هاردوير + سوفت وير /مبرمج هواتف ذكية  سوفت وير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Electrical and Electronic Appliance Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة ادوات كهربائيه والكترونيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Bulldozer Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق جرافة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8537 -- Livestock Worker (Sheep, Dairy Cow, and Calf Breeder)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مربي اغنام وابقار حلوب وعجول'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8537
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (Laboratory Diagnostic Doctor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طبيب تشخيص مخبري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);


UPDATE candidate_occupation co
SET occupation_id = 10004 -- Stonemason (Stone Lathe Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مخرطة حجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10004
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Plumber and Ironing Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سباك وعامل كوي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Plumber and Ironing Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سباك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Marble and Granite Cutting and Processing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عمل وقص الرخام والغرانيت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tiles and Electrical Welding - assumed as craft work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'قرميد ولحم الكهرباء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Surveillance Camera Installation Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تركيب كاميرات مراقبة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8521 -- Finance professional (Financial and Banking Sciences)
WHERE TRIM(LOWER(co.migration_occupation)) = 'علوم مالية و مصرفية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8521
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Cultural Center Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مركز ثقافي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer and Laptop Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة أجهزة الحاسوب و اللاب توب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Transporter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'transporter'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Assistant Warehouse Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد امين مستودع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Project Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مشروع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (In Private and Government Laboratories - assumed lab work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'في مختبرات اهليه وحكوميه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Electronics Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانه الكترونيات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Freelance Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان حر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Assistant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8690 -- Sales professional (Travel Ticket Sales Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مبيعات تذاكر سفر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8690
);

UPDATE candidate_occupation co
SET occupation_id = 8643 -- Graphic designer (3D Character Design)
WHERE TRIM(LOWER(co.migration_occupation)) = '3d character design'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8643
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Pricing and Contracting Engineering)
WHERE TRIM(LOWER(co.migration_occupation)) = 'pricing and contracting engineering'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'management'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Animator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منشط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);


UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer in UN Organizations to Help Refugees)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع في منظمات تابعة للأمم المتحدة لمساعدة الاجئين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);


UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Human Development Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب تنمية بشرية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8610 -- Concrete placer, finisher or related (Concrete Blacksmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداد باطون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8610
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decorative Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان دكور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Decorative Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان ديكور موبليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Training on Different Ticketing GDS - assumed administrative role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'training on different ticketing gds'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8603 -- Surveyor, land (Land Surveyor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساح أراضي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8603
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Central Heating)
WHERE TRIM(LOWER(co.migration_occupation)) = 'التدفئة المركزية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Smartphone Maintenance Software and Hardware)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة هوتف ذكية سوفت وير و هارد وير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);


UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (IT/Accountant Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'it/accountant assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Operations Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس تشغيل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Repair Shop)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محل صيانه كمبيوتر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Packaging Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشغل ماكينة تعبئه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (First Aid)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اسعاف اولي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);


UPDATE candidate_occupation co
SET occupation_id = 8685 -- Professor or Lecturer (Assistant Professor/University Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assistant professor/university teacher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8685
);

UPDATE candidate_occupation co
SET occupation_id = 8690 -- Sales professional (Sales Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير مبيعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8690
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Career Guidance Counselor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'career guidance conselor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Network Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'network enginner'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Projects Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'projects coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8630 -- Farmer (crop and vegetable) (Agriculture)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الزراعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8630
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Call Center)
WHERE TRIM(LOWER(co.migration_occupation)) = 'call center'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Administration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الادارة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychologist Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = ',أخصائية نفسية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Barber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator/Interpreter/Transcriber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translator/interpreter/trasncriber'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Sanitary Tools - assumed plumbing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادوات صحية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Oil and Machinery Parts Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع زيوت وقطع واليات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Female Volunteer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Monitoring and Evaluation M & E Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مراقبة وتقييم m & e officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teaching)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدريس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer with Syrian Arab Red Crescent)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع بلهلال الاحمر العربي السوري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Ice Cream Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل بوظة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Communication Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'communication officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Proofreading)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدقيق لغوي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خدمة العملاء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Shift Leader Concierge)
WHERE TRIM(LOWER(co.migration_occupation)) = 'shift leader concierge'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Student Service)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خدمة طلابية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Mathematics Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلمة رياضيات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Service Desk Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'service desk coordinator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);


UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Voiceover)
WHERE TRIM(LOWER(co.migration_occupation)) = 'التعليق الصوتي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Ticket Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف تذاكر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Snack Teacher - Fast Food)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم سناك (وجبات سريعة)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Kindergarten Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلمة روضة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 9436 -- Housewife (Housewife)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ربة منزل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9436
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Assistant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد  شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Men’s Barber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاق رجالي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Sales Representative)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندوب مبيعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Programs Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق برامج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Handicrafts, Puppet Theater, and Children’s Activities)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اشغال يدوية .مسرح دمى وانشطة اطفال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Female Community Volunteer with UNHCR)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوعه مجتمعيه.مع المفوضيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Soap Making)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة الصابون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Electronics Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الالكترونيات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);


UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Drilling - Cementing Technical Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'drilling (cementing) - technical engineer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Field Manager Inside Refugees Camp)
WHERE TRIM(LOWER(co.migration_occupation)) = 'fild manger inside refugees camp'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8505 -- Computer programmer (Programmer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'programmer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8505
);

UPDATE candidate_occupation co
SET occupation_id = 8685 -- Professor or Lecturer (Lecturer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'lecturer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8685
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Stamped Concrete)
WHERE TRIM(LOWER(co.migration_occupation)) = 'stamped concrete.'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Communication Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'communication engineer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (Stringed Instruments - unclear, possibly a typo or unrelated)
WHERE TRIM(LOWER(co.migration_occupation)) = 'وتار مصارين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Steam Turbine Operation Experience)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خبرة تشغيل عنفات بخارية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Printing and Photoshop Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني طباعة وفوتوشوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8701 -- Statistician (Statistics)
WHERE TRIM(LOWER(co.migration_occupation)) = 'احصاء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8701
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Board Decoration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكور جبسمبورد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Cheese and Dairy Production)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اجبان والبان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chicken Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف.دجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (First Aid Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب اسعاف اولي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Satellite Technician and Cable/Internet Line Installation and Computer Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ستالايت وتمديد خطوط ستالايت وانترنت وصيانة حواسيب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Hummus and Foul Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم حمص وفول'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Website Administrator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'website administrator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Administrative Job)
WHERE TRIM(LOWER(co.migration_occupation)) = 'administrative job'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Selling and Producing Dairy and Cheese)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بيع وصناعة الالبان والاجبان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Driver of Engineering Equipment and Heavy Machinery - Excavator and Bulldozers)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق معدات هندسية وأليات ثقيلة حفارة مجنزرة وجرافات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Painter with Professional Experience in 3D Panels)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان خبرة مهنية لوحات 3d'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Motor vehicle driver (cab, truck, or other) (Freight Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق شحن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة حواسيب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Web Developer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'web developer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Support and Facilitation for Children)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دعم وتيسير اطفال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Trader)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تاجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tile Polishing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جلى بلاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Security Guard and Protection)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سكيورتي امن وحماية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (HR Manager and Consultant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'hr manager and consultant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8622 -- Driver and mobile-plant operator (Heavy Bulldozer Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق جرافة ثقيلة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8622
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Stone Sculptor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحات حجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Glass Installation and Removal for All Vehicles)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فك وتركيب زجاج جميع السيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Shawarma and Cocktail Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم شاورما و كوكتيل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Clothing Shop)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محل ألبسة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Laboratory technician (Laboratory Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'laboratory technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);



UPDATE candidate_occupation co
SET occupation_id = 8590 -- Waiter or bartender (Waiter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كرسون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8590
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wood Sculptor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحات اخشاب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة اجهزة الخلوي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Member/Volunteer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'member/ volunteer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);


UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Building Guard)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ناطور بناء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (English Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'english translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Arabic Shawarma Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم شاورما عربي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Falafel and Block Press Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم فلافل ومكبس بلوك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (CNC Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشغل ماكينات cnc'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Technical Supervisor/Operation and Control)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ملاحظ فني/تشغيل وسيطرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Popular Food Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم أكلات شعبية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Barber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (Freelance Work - too vague)
WHERE TRIM(LOWER(co.migration_occupation)) = 'أعمال حره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Restaurants ... Menu ... Cardboard Printing Field, Assistant Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطاعم ...المينو ....في مجال الطباعه الكرتون    مساعد شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Coffee, Nuts, Chocolate, Thyme, and Medicinal Herbs Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع قهوة ومكسرات وشوكلا وزعتر والشاب طبية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Supervisor of Aluminum, Carton Wall, Glass, and Aluminum Composite Panel Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشرف تركيب وجهات الامنيوم كارتنوول وزجاج والي كيبوند'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Pastries)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معجنات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Drawing on Glass and Mirrors)
WHERE TRIM(LOWER(co.migration_occupation)) = 'drawing on glass and mirrors'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة اجهزة موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Al Jalil Association - assumed NGO work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'al jalil association'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8700 -- Researcher (Researcher in Public Health Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'researcher in public health field.'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8700
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Paint - English variant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'paint'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Electricity)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كهرباء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Cheese and Dairy Factory)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معمل اجبان والبان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);


UPDATE candidate_occupation co
SET occupation_id = 8493 -- Blacksmith (Blacksmithing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداده'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8493
);


UPDATE candidate_occupation co
SET occupation_id = 8493 -- Blacksmith (Blacksmithing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8493
);


UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Web Developer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'web developer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Camera, Protection, and Alarm Device Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب كمرات و حماية و اجهزة انذار'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Media Worker in Local Council in Tel Shehab / Volunteer Work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعلامي في المجلس المحلي في بلدة تل شهاب/ عمل تطوعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (A Host and Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'a host and translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (House Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان بيوت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decoration and Painting Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني ديكور ودهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Surveillance and Alarms Systems)
WHERE TRIM(LOWER(co.migration_occupation)) = 'surveillance and alarms systems'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Information Field Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'information filed assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Technical Support)
WHERE TRIM(LOWER(co.migration_occupation)) = 'technical support'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Food Industry Engineer - Sugar Factories)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس صناعات  غذائيه  معامل  السكر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Engineer - Network Configuration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'engineer (network configuration )'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Nurse (Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'nurse'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Head of Office at Syrian Arab Red Crescent Branch in Tartous)
WHERE TRIM(LOWER(co.migration_occupation)) = 'رئيس ديوان لدى فرع الهلال الأحمر العربي السوري في طرطوس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Espresso Coffee Machine Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصليح مكنات القهوة اكس برسو'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Securit Glass Installation Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم تركيب زجاج سيكوريت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Engineering)
WHERE TRIM(LOWER(co.migration_occupation)) = 'engineering'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Plywood and Manqoush Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم مبلكسين ومنكوش'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10018 -- Other Health Associate Professionals (Assistant Anesthesiologist or Anesthesia Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد طبيب تخدير او فني تخدير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10018
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Car Wash, Oil Change, and Tire Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'غسيل سيارات وغيار زيت و بنشرجي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8643 -- Graphic designer (Designer and Laser Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصمم ولعمل على مكنات ليزر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8643
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Employment Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'employment officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Petrol Station Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير محطة بترول'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Laboratory technician (Dental Laboratory)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مختبر اسنان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decorative Painting Teacher Skilled in Furniture Spraying)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ديكورات دهان ومتقن رش موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (IT Business Liaison/Banking Functional Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'it business liaison/banking functional specialist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Media)
WHERE TRIM(LOWER(co.migration_occupation)) = 'media'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Assistant - misspelling assumed)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assestant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (General Maintenance, Satellite Programming, and Truck Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة عامة وبرمجة ستالايت وسائق شاحنة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (In Pharmacology and Medical Alternatives, Plastic Arts)
WHERE TRIM(LOWER(co.migration_occupation)) = 'في الفارماكولوجيا و البدائل الطبية، فنات تشكيلي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 8700 -- Researcher (Research Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8700
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Medical Equipment Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'medical equipment manufacturing'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);


UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Trainer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'trainer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8650 -- Physiotherapist (Physiotherapy)
WHERE TRIM(LOWER(co.migration_occupation)) = 'العلاج الطبيعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8650
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Sales Representative - typo corrected)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندوب مببعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Logistics and Warehouses)
WHERE TRIM(LOWER(co.migration_occupation)) = 'logistics and wearhouses'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Information Management Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'information management officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8545 -- Metal worker (Lathe Operator for Metal Turning and Blacksmithing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طورنجي خراطة معادن وحدادة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8545
);

UPDATE candidate_occupation co
SET occupation_id = 10021 -- Other Stationary Plant and Machine Operators (Plastic Blowing and Injection Machine Operator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تشغيل ماكينات نفخ وحقن البلاستيك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10021
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة كمبيوتر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Communications Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

-- Eight
UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Women’s Hairdresser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافيره نسائية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8630 -- Farmer (crop and vegetable) (Agriculture)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الزراعه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8630
);


UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service - typo corrected)
WHERE TRIM(LOWER(co.migration_occupation)) = 'customar service'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoes - assumed shoemaking)
WHERE TRIM(LOWER(co.migration_occupation)) = 'احذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافيرة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);


UPDATE candidate_occupation co
SET occupation_id = 9443 -- Unknown (Ironing - unclear context, possibly tailoring or unrelated)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9443
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser - variant spelling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوفيره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoes - Arabic variant, assumed shoemaking)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الاحذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكورات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Maintenance - general)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8630 -- Farmer (crop and vegetable) (Farmer - typo corrected)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مزراع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8630
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Sprayer - typo assumed "بحاخ مبيلة")
WHERE TRIM(LOWER(co.migration_occupation)) = 'بحاخ مبيله'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Stone Carving)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نحت حجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Sprayer, typo assumed "بحاح مبيليه")
WHERE TRIM(LOWER(co.migration_occupation)) = 'بحاح مبيليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Sanitary and Drainage Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تمديت صحيه وشفاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Home Building Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكورات بناء في المنازل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Heating and Cooling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدفأه وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Entrepreneurship)
WHERE TRIM(LOWER(co.migration_occupation)) = 'رياده الاعمال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Mechanical Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة ميكانيكية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (musician, actor/actress, etc.) (Film Industry: Acting, Filming, Sketches)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعه الافلام تمثيل تصوير اسكتشات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8493 -- Blacksmith (Forge Blacksmith)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداد فرنجي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8493
);

UPDATE candidate_occupation co
SET occupation_id = 8577 -- Accountant (Accounting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محاسبة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8577
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Communications Worker - typo assumed "اتصلات")
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل اتصلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Roaster and Rahat Lokum Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم محمص وراحة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);


UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (AC, Refrigeration, Cooling Rooms, Heating, Radiators, Car AC, and Ventilation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكييف تبريد وغرف تبريد وتدفئة وشوفاج وتكييف سيارات وتهوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Silk Printing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طباعة حرير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Tailoring)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خياطة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Women’s Beautician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تجميل نسائي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Painter - typo variant "داهن")
WHERE TRIM(LOWER(co.migration_occupation)) = 'داهن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Jewelry Boxes)
WHERE TRIM(LOWER(co.migration_occupation)) = 'علب مجوهرات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Design)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم احذيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Manufacturing Molds for Leather and Shoe Sole Parts)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع قوالب قطع جلود و مشمع و نعل الاحذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychological Support)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الدعم النفسي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wood Engraving via Laser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حفر على خشب عن طريق ليزر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Legal and Financial Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير قانوني ومالي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Embroidery)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تطريز'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Factory - typo assumed "مصنع اخذاء")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصنع اخذاء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teaching Student)
WHERE TRIM(LOWER(co.migration_occupation)) = 'teaching student'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8545 -- Metal worker (Stainless Steel, Brass, and Iron Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم استانلس وناس وحديد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8545
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Makeup Artist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكياج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Shoe Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائعه احذيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flexo Printing Press)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطبعة فليكسو'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum and Glass Accessories)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اكسسوارت المينيوم وزجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Seller in a Shoe Store)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع بمحل احذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Upholstery)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تنجيد سيارت'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Car Body Straightening and Blacksmithing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تجليس بودي حداد سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Car Mechanic and Turner - typo assumed "مكنيان ودوران")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكنيان ودوران سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Communications Technician and Accounting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني اتصالات ومحاسبة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم احذيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wood Painter - Bedrooms and Doors, Furniture)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان اخشاب (غرف نوم وابواب) موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة اجهزة خلوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Networks and Systems Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس شبكات وانظمه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translator and Interpreter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'translator and interpreter'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Restoration Expert - assumed craft-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'restoration expert'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (English Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرس انكليزي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Chief Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مترجم رئيسي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Humanitarian Agency)
WHERE TRIM(LOWER(co.migration_occupation)) = 'humanitarian agency'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);


UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources specialist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (Undertook a Commitment for University Faux Ceiling - unclear occupation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اخذت تعهد بعمل الفورسيلينغ للجامعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);

UPDATE candidate_occupation co
SET occupation_id = 8782 -- Engineer (civil) (Civil Engineer - typo corrected)
WHERE TRIM(LOWER(co.migration_occupation)) = 'civil enginer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8782
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Mechanic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ميكانيكي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8690 -- Sales professional (Marketing Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشرف تسويق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8690
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Import and Export)
WHERE TRIM(LOWER(co.migration_occupation)) = 'استيراد وتصدير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Carpenter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نجار مبيليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoes - assumed shoemaking)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حذاء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoemaker - variant spelling "كندرجي")
WHERE TRIM(LOWER(co.migration_occupation)) = 'كندرجي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Manufacturing - All Types)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصنيع الاحذية جميع انواعه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoemaker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صانع احذيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoes)
WHERE TRIM(LOWER(co.migration_occupation)) = 'احذيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Business Analyst)
WHERE TRIM(LOWER(co.migration_occupation)) = 'business analyst'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Sprayer - variant "بخاخ مبيليه")
WHERE TRIM(LOWER(co.migration_occupation)) = 'بخاخ مبيليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teaching Assistant - corrected to "Assistance")
WHERE TRIM(LOWER(co.migration_occupation)) = 'teaching assistance'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Carpenter - variant "نجار موبيليا")
WHERE TRIM(LOWER(co.migration_occupation)) = 'نجار موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Leatherwork: Women’s Bags and Purses)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جلديات جزادين نسائيه حقائب جلد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Nurse (Nursing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تمريض'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Women’s Tailoring - assumed "قشاطات" as tailoring-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'قشاطات نسائية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8489 -- Unknown (Mechanics - unclear context, assumed typo "مكنسين")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكنسين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8489
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (Theatrical Performance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'theatrical performance'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8493 -- Blacksmith (Forge Blacksmithing - variant "حدادة فرنجية")
WHERE TRIM(LOWER(co.migration_occupation)) = 'حدادة فرنجية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8493
);

UPDATE candidate_occupation co
SET occupation_id = 8643 -- Graphic designer (Fashion Design)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصميم ازياء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8643
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Decoration and Painting Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ديكور جبس ودهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Trainer - female form "مدربة")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدربة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 10017 -- Other Teaching Professionals (Trainer - female form "مدربة")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10017
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Sprayer and Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بخاخ ، دهان موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Journalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صحافي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouse Keeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'امين مستودع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Restaurants)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطاعم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Restaurant Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مجال المطاعم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Customer Service Center Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادارة مركز خدمات الزبائن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (School Administration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادارة مدرسية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);


UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانه الجوال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Computer Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة حاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Tiling - assumed "بليط" as typo for tiling-related work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بليط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Assistant Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساعد مهندس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Vegetable Trader)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تاجر خضاره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Decorations, Painting, and Carpentry Fields)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعمل في مجال الديكورات   ومجال الدهان   ومجال النجاره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Women’s Lingerie and Underwear)
WHERE TRIM(LOWER(co.migration_occupation)) = 'لانجري نسائي البسه داخليه نسائيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Electricity and Car Computer Diagnostics)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كهرباء وفحص كمبيوتر السيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Soap Making)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعه الصابون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8630 -- Farmer (crop and vegetable) (Chicken Farmer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مزارع دجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8630
);

UPDATE candidate_occupation co
SET occupation_id = 8690 -- Sales professional (Electronic and Multi-Level Marketing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'التسويق الالكتروني والمتعدد المستويات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8690
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة الأحذية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8677 -- Personal care worker (Housekeeper)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدبرت منزل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8677
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter and Sprayer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان وبخ موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Stone Sawmill Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منشره حجر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Car Mechanic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكانيكي سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Humanitarian Work)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الأعمال الانسانية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Laboratory technician (Health Sciences/Medical Testing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'علوم صحية /تحاليل طبية/'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);

UPDATE candidate_occupation co
SET occupation_id = 8685 -- Lawyer (Lawyer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اعمل محامي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8685
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Wholesale Trader)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تاجر جمله'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Nurse (Nurse - female form "ممرضه")
WHERE TRIM(LOWER(co.migration_occupation)) = 'ممرضه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Receptionist/Front Office Agent)
WHERE TRIM(LOWER(co.migration_occupation)) = 'receptionist/front office agent'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Trade)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تجاره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافيره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Household Electrical Appliance Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الادوات الكهربائية المنزلية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter - variant "دهان مبيليه")
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان مبيليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8650 -- Physiotherapist (Physiotherapist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'physiotherapist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8650
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ألمنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (Actor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ممثل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Teacher - assumed repair-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم خلويات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Car Key Maker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'car key maker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (Emergency Doctor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طبيب طوارئ'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Building Painter and Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان مباني وديكورات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8545 -- Metal worker (Lathe Operator for Collars - assumed "مخرطه كولاسات")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مخرطه كولاسات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8545
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Home Furniture Upholstery)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تنجيد مفروشات منزلي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (AC and Refrigeration Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني تكييف وتبريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile and Computer Maintenance Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم صيانة موبايلات وكمبيوتر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Electrical Control for Elevators)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تحكم كهربائي للمصاعد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Board and Plaster Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكور جبسم بورد وجفصين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Tiler and Stone/Ceramic Cladding)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مبلط وتلبيس حجر وسيراميك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8782 -- Engineer (civil) (Surveyor - assumed related to civil engineering)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مساح surveyor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8782
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Nurse (Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ممرض'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Device Maintenance and Programming, 15 Years Experience)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة وبرمجة اجهزة الخليوي خبرة 15 عام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Driver (Delivery)
WHERE TRIM(LOWER(co.migration_occupation)) = 'delivery'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Manager of Human Rights and Humanitarian Organization)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير منظمة حقوقية وانسانية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Painting Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم دهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8675 -- Personal and Protective Service Worker (Campus Housing Supervisor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مشرف سكن كامبوس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8675
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Actros Car Repair)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تصليح سيارات اكتروس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);


UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Quality Assurance Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'quality assurance officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Employee in a Mobile Company - assumed IT-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف في شركة موبيلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Restaurant Hall Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف صالة بمطعم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Arabic and Western Furniture Carpenter and Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نجار موبيلا عربي وافرنجي وديكورات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Warehouses Administration)
WHERE TRIM(LOWER(co.migration_occupation)) = 'warehouses administration'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Assessment and Distribution - assumed humanitarian-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assessment and distribution'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

-- Tenth

UPDATE candidate_occupation co
SET occupation_id = 8700 -- Researcher (Researcher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'researcher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8700
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Volunteer at International Child Rescue Association)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متطوع في جمعية انقائ الطفل الدولية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);

UPDATE candidate_occupation co
SET occupation_id = 8577 -- Accountant (Account Auditing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدقيق الحسابات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8577
);

UPDATE candidate_occupation co
SET occupation_id = 8700 -- Researcher (Human Rights Researcher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'باحث في حقوق الانسان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8700
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Relief and Sustainable Development)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الاغاثة والتنمية المستدامة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);


UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (Art - Music)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الفن .. موسيقى'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Administrative Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول اداري'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Employee at Kafa Violence and Exploitation Organization)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف في منظمة كفى عنف واستغلال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Refugee Camp Management and Relief Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ادارة تجمعات لجوء ومدير اغاثي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Assistant Professor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'assistant professor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Media - Journalism)
WHERE TRIM(LOWER(co.migration_occupation)) = 'إعلام صحافة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الموبايلات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Organization Concerned with Humanitarian Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منظمة تعنى بالمجال الإنساني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);


UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Worked in Education, Teaching, and Psychological Support)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عملت في التعليم والتدريس و الدعم النفسي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (AC, Refrigeration, Car AC, Cooling Rooms, and Hot Air Heating)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تكييف وتبريد وتكييف سيارة وغرف تبريد وتدفئة بلهواء الساخن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Wireless Communications - assumed "اتصالات سليكة" typo)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اتصالات سليكة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (House Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان منازل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة الهواتف الخلوية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Educational Projects Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق مشاريع تعليمية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Operations and Accounts Manager)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدير العمليات والحسابات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (Creative Arts Specialist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مختص فنون ابداعية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Aluminum Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8677 -- Personal care worker (Children’s Activities Facilitator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تنشيط اطفال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8677
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Bank - assumed banking-related role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بنك'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Embroidery)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تطريز'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Translation - Editing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ترجمة ـ تحرير'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Men’s Barber)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حلاقة رجالية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Eastern and Western Restaurant Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم مطاعم شرقي غربي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);


UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (IT)
WHERE TRIM(LOWER(co.migration_occupation)) = 'it'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Ports Manager - typo corrected "Ports Managet")
WHERE TRIM(LOWER(co.migration_occupation)) = 'ports managet'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Laboratory technician (Medical Laboratory Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تقني مخبر تحاليل طبية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);

UPDATE candidate_occupation co
SET occupation_id = 8677 -- Personal care worker (Facilitator in Child Protection, Psychological, and Social Support)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منشطة بمجال حماية الطفل والدعم النفسي والاجتماعي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8677
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (English Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'english teacher'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Deputy Production Line Manager for Car Assembly)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نائب مدير خط انتاج تجميع سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8700 -- Researcher (Field Research)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بحث ميداني'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8700
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Monitoring and Evaluation Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول مراقبة و تقييم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Government Employee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'موظف حكومي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8646 -- Healthcare professional (medical doctor) (General Medicine)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طب عام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8646
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Human Rights)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حقوق الانسان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Tiling)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بلاط'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Teacher and Supervisor in Electricity Field)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ومشرف في مجال الكهرباء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 10019 -- Other Personal Services Workers (Organizer Volunteer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'organizer volunteer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10019
);


UPDATE candidate_occupation co
SET occupation_id = 8674 -- Performing artist (DJ)
WHERE TRIM(LOWER(co.migration_occupation)) = 'dj'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8674
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Refugee Shelter Center)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مركز ايواء اللاجئين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Teacher and Supervisor at Chocolate Factory)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم ومشرف على معمل شكولا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Education)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تعليم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Arabic Calligrapher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خطاط عربي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Youth Development)
WHERE TRIM(LOWER(co.migration_occupation)) = 'youth development'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Nurse (Nurse)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ممرض'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter - typo "دهان مبويليا")
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان مبويليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Private Shop Owner)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صاحب محل خاص'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8602 -- Blacksmith (Concrete Carpenter + Concrete Ironworker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نجار باطون + حديد باطون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8602
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wallpaper Installer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مورق جدران'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8602 -- Blacksmith (Concrete Ironworker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حداد باطون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8602
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wall Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان جدران'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wall Finisher - "مورق" assumed wall smoothing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مورق ( تمليس جدران )'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychological Support and Child Activities)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دعم نفسي وتنشيط اطفال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8629 -- Engineer (mechanical) (Medical Equipment Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'medical equipment technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8629
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Printing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'طباعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Phone Maintenance Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة هواتف محمولة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Project Coordination)
WHERE TRIM(LOWER(co.migration_occupation)) = 'project coordination'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Transaction Processor - assumed "معاقب معاملات" typo)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معاقب معاملات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Journalist)
WHERE TRIM(LOWER(co.migration_occupation)) = 'journalist'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Executive Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'executive management'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Eastern and Western Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف شرقي غربي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8643 -- Graphic designer (Design - assumed "ديزان" typo)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديزان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8643
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Coordinator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'منسق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);


UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teaching Quran, Reading, and Writing in Arabic)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تعليم القران والقراءة والكتابة باللغة العربية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Medical Representative)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندوب طبي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Expert in Painting and Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'خبير في مجال الدهان والديكورات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Transaction Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تسيير معاملات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Hot Air Heating)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدفئة بالهواء الساخن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Air Heating and Water Heaters)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تدفئة هوائية وسخانات ماء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Gypsum Board Decorations and False Ceilings)
WHERE TRIM(LOWER(co.migration_occupation)) = 'ديكورات جبسن بورد واسقف معلقة فور سيلنغ'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Music Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرس موسيقى'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Men’s Awareness Sessions)
WHERE TRIM(LOWER(co.migration_occupation)) = 'جلساات توعية للرجال'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Delivery Technician - assumed technical delivery role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني توصيل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance Technician at Nokia Agency in Syria)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني صيانة جولات بوكيل شركة نوكيا بسوريا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (American Kitchens and Aluminum Carpentry)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مطابخ امريكيه ومنجور المنيوم'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Sales Representative)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مندوب مبيعات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 10015 -- Mechanic, engine: diesel (except motor vehicle) (Mobile Maintenance)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صيانة موبايل'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 10015
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Painting Technician)
WHERE TRIM(LOWER(co.migration_occupation)) = 'فني دهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Sheep Intestine Processing for Surgical Operations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مصران غنم للعمليات الجراحيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Women’s Bags)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شنط نسائي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان الموبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Management)
WHERE TRIM(LOWER(co.migration_occupation)) = 'management'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Floppy Disk Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة القرص المرن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);


UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Flower Arrangement Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم تنسيق زهور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Sports Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مدرس رياضة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Full Construction Contracting)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تعهدات بناء كامله'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Glass Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل زجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Glass Installation)
WHERE TRIM(LOWER(co.migration_occupation)) = 'تركيب زجاج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الدهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);
UPDATE candidate_occupation co
SET occupation_id = 8547 -- Driver (Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'سائق'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);

UPDATE candidate_occupation co
SET occupation_id = 8602 -- Blacksmith (Concrete Formwork)
WHERE TRIM(LOWER(co.migration_occupation)) = 'غلاف باطون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8602
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Textiles)
WHERE TRIM(LOWER(co.migration_occupation)) = 'النسيج'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Shoe Manufacturing)
WHERE TRIM(LOWER(co.migration_occupation)) = 'صناعة احذيه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Clothing Company)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شركه ملابس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Offshore Operations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'offshore operations'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);


UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Household Goods Seller)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بائع ادوات منزليه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Wooden Flooring Parquet)
WHERE TRIM(LOWER(co.migration_occupation)) = 'باركي  ارضيات خشب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher - female form "معلمه")
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلمه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Western Bread Chef)
WHERE TRIM(LOWER(co.migration_occupation)) = 'شيف خبز افرنجي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8567 -- Social work professional (Event Organizer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'event organizer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8567
);

UPDATE candidate_occupation co
SET occupation_id = 8691 -- Sales representatives/merchant/trader (Selling - Clothing Sales)
WHERE TRIM(LOWER(co.migration_occupation)) = 'البيع..بيع الملابس'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8691
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (English Teacher - assumed "انسه انكليزه" typo)
WHERE TRIM(LOWER(co.migration_occupation)) = 'انسه انكليزه'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Editor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'محرر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Volunteer Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مترجم متطوع'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (General Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان عام'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Decoration Engineer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مهندس ديكور'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Sprayer and Painter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'بخاخ ودهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (False Ceilings and All Painting Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اسقف مستعاره وجميع ديكورات الدهان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Interior and Exterior House Painter and Decorations)
WHERE TRIM(LOWER(co.migration_occupation)) = 'دهان داخل وخارج المنازل  وديكورات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Women’s Hairdresser)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافيرة نسائية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8556 -- Plumber or pipe fitter (Everything Related to Construction)
WHERE TRIM(LOWER(co.migration_occupation)) = 'في كل شيئ يتعلق بلبناء'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8556
);

UPDATE candidate_occupation co
SET occupation_id = 8545 -- Metal worker (Metal Lathe Operator and Metal Welding)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مخرطة معادن وتلحيم معادن'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8545
);

UPDATE candidate_occupation co
SET occupation_id = 8618 -- Laboratory technician (Dental Laboratory)
WHERE TRIM(LOWER(co.migration_occupation)) = 'مختبر أسنان'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8618
);

UPDATE candidate_occupation co
SET occupation_id = 8530 -- Hairdresser, barber, beautician or related (Hairdresser - variant "كوافيرا")
WHERE TRIM(LOWER(co.migration_occupation)) = 'كوافيرا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8530
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Real Estate and Administrative Affairs)
WHERE TRIM(LOWER(co.migration_occupation)) = 'الامور العقارية والادارية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8543 -- Mechanic (electrical) (Elevators)
WHERE TRIM(LOWER(co.migration_occupation)) = 'المصاعد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8543
);

UPDATE candidate_occupation co
SET occupation_id = 8785 -- Engineer (other) (Architectural Engineering Trainee)
WHERE TRIM(LOWER(co.migration_occupation)) = 'متدرب هندسة معمارية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8785
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Furniture Sprayer - "بخاخ موبيليا")
WHERE TRIM(LOWER(co.migration_occupation)) = 'بخاخ موبيليا'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8602 -- Blacksmith (Concrete Carpenter)
WHERE TRIM(LOWER(co.migration_occupation)) = 'نجار باطون'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8602
);

-- 11th
UPDATE candidate_occupation co
SET occupation_id = 9443 -- Unknown (Freelance Worker - too vague)
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل حر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9443
);

UPDATE candidate_occupation co
SET occupation_id = 0 -- Unknown (Mechanics - unclear context, assumed typo "مكنسين")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مكنسين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 0
);

UPDATE candidate_occupation co
SET occupation_id = 8677 -- Pharmacist (Pharmacist Technician - assumed assistant role)
WHERE TRIM(LOWER(co.migration_occupation)) = 'pharmacist technician'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8677
);

UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Media)
WHERE TRIM(LOWER(co.migration_occupation)) = 'media'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);


UPDATE candidate_occupation co
SET occupation_id = 9443 -- Unknown (Free - assumed "حر" as freelance, too vague)
WHERE TRIM(LOWER(co.migration_occupation)) = 'حر'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9443
);


UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Teacher of English in High School)
WHERE TRIM(LOWER(co.migration_occupation)) = 'teacher of english in high school'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);

UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Student Assistant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'student assistant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);

UPDATE candidate_occupation co
SET occupation_id = 8614 -- Craft and related trades worker (Contracted False Ceiling Work for University - assumed construction-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'اخذت تعهد بعمل الفورسيلينغ للجامعة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8614
);

UPDATE candidate_occupation co
SET occupation_id = 8689 -- Safety, health & quality inspector (Inspector)
WHERE TRIM(LOWER(co.migration_occupation)) = 'inspector'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8689
);

UPDATE candidate_occupation co
SET occupation_id = 9443 -- Unknown (Multi-Talented - too vague "متعدد المواهب")
WHERE TRIM(LOWER(co.migration_occupation)) = 'متعدد المواهب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9443
);

UPDATE candidate_occupation co
SET occupation_id = 8705 -- Teacher (Fourth Grade Elementary Teacher)
WHERE TRIM(LOWER(co.migration_occupation)) = 'معلم صف رابع ابتدائي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8705
);


UPDATE candidate_occupation co
SET occupation_id = 9443 -- Freelance (Freelancer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'freelancer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9443
);

UPDATE candidate_occupation co
SET occupation_id = 8784 -- Software engineer (Network Engineer - assumed IT/networking focus)
WHERE TRIM(LOWER(co.migration_occupation)) = 'network enginner'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8784
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Intestine String Maker - assumed "وتار مصارين" as surgical string production)
WHERE TRIM(LOWER(co.migration_occupation)) = 'وتار مصارين'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 9443 -- Freelance (Freelance Work - "أعمال حره")
WHERE TRIM(LOWER(co.migration_occupation)) = 'أعمال حره'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9443
);


UPDATE candidate_occupation co
SET occupation_id = 8712 -- Writer (Freelance Subtitle Translator)
WHERE TRIM(LOWER(co.migration_occupation)) = 'freelance subtitle translator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8712
);


UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Supply Chain and Logistics)
WHERE TRIM(LOWER(co.migration_occupation)) = 'supply chain and logistics'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Red Team | Vulnerability Researching | Attack Simulation - IT security focus)
WHERE TRIM(LOWER(co.migration_occupation)) = 'red team | vulnerability researching | attack simulation'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);

UPDATE candidate_occupation co
SET occupation_id = 8663 -- Manufacturing labourer (Ordinary Worker - "عامل عادي")
WHERE TRIM(LOWER(co.migration_occupation)) = 'عامل عادي'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8663
);

UPDATE candidate_occupation co
SET occupation_id = 8571 -- Tailor, dressmaker or hatter (Tailor)
WHERE TRIM(LOWER(co.migration_occupation)) = 'tailor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8571
);

UPDATE candidate_occupation co
SET occupation_id = 9441 -- Business Owner (Business owner, maintenance, artist painter, plasterer - primary role assumed)
WHERE TRIM(LOWER(co.migration_occupation)) = 'business owner, maintenance, artist painter, plasterer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 9441
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources Manager - typo corrected "Resouces")
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resouces manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8786 -- Human resource manager (Human Resources Manager - typo corrected "Resouces")
WHERE TRIM(LOWER(co.migration_occupation)) = 'human resources manager'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8786
);

UPDATE candidate_occupation co
SET occupation_id = 8547 -- Driver (Driver)
WHERE TRIM(LOWER(co.migration_occupation)) = 'driver'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8547
);


UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef Shawarma and Burger)
WHERE TRIM(LOWER(co.migration_occupation)) = 'chef shawarma and burger'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);


UPDATE candidate_occupation co
SET occupation_id = 8484 -- Administrative assistant (Mail Correspondent - "مراسل بريد")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مراسل بريد'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8484
);


UPDATE candidate_occupation co
SET occupation_id = 8499 -- Business professional not elsewhere classified (Strategic Consultant)
WHERE TRIM(LOWER(co.migration_occupation)) = 'strategic consultant'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8499
);

UPDATE candidate_occupation co
SET occupation_id = 8535 -- Information technology professional (Executive Officer in a Telecom Company - "مسؤول تنفيذي في شركة اتصالات")
WHERE TRIM(LOWER(co.migration_occupation)) = 'مسؤول تنفيذي في شركة اتصالات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8535
);


UPDATE candidate_occupation co
SET occupation_id = 8689 -- Safety, health & quality inspector (Engineering Inspection (RT (X-ray)) - "فحص هندسي")
WHERE TRIM(LOWER(co.migration_occupation)) = 'فحص هندسي (rt (x_ray)'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8689
);

UPDATE candidate_occupation co
SET occupation_id = 8649 -- Optometrist (Optic - assumed optometry-related)
WHERE TRIM(LOWER(co.migration_occupation)) = 'optic'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8649
);

UPDATE candidate_occupation co
SET occupation_id = 8703 -- Student (University Student with Programming and Computer Experience - "طالب جامعي لدي خبرة في البرمجة والحاسوب")
WHERE TRIM(LOWER(co.migration_occupation)) = 'طالب جامعي لدي خبرة في البرمجة والحاسوب'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8703
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (Electrical)
WHERE TRIM(LOWER(co.migration_occupation)) = 'electrical'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Worker at CIS Institutes Group for Cleaning and Maintenance - "أعمل في مجموعة معاهدCIS عامل تنضيف وصيانة")
WHERE TRIM(LOWER(co.migration_occupation)) = 'أعمل في مجموعة معاهدcis عامل تنضيف وصيانة'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8516 -- Electrician (House Electrician, Welder, Car Tuning - "كهرباء منازل حداد لحام دوزان سيارات" - primary role assumed)
WHERE TRIM(LOWER(co.migration_occupation)) = 'كهرباء منازل  حداد لحام    دوزان سيارات'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8516
);

UPDATE candidate_occupation co
SET occupation_id = 8648 -- Nurse (Nursing Instructor - "Nursing instuctor")
WHERE TRIM(LOWER(co.migration_occupation)) = 'nursing instuctor'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8648
);

UPDATE candidate_occupation co
SET occupation_id = 8612 -- Cook (Chef - typo "شف")
WHERE TRIM(LOWER(co.migration_occupation)) = 'شف'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8612
);

UPDATE candidate_occupation co
SET occupation_id = 8528 -- General manager (own or small business) (Supervisor of Workers at Interior Decoration Company - "supervisor مشرف عمال شركة ديكورات داخلية")
WHERE TRIM(LOWER(co.migration_occupation)) = 'supervisor مشرف عمال شركة ديكورات داخلية'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8528
);

UPDATE candidate_occupation co
SET occupation_id = 8500 -- Butcher (Butcher Worker)
WHERE TRIM(LOWER(co.migration_occupation)) = 'butcher worker'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8500
);

UPDATE candidate_occupation co
SET occupation_id = 8544 -- Mechanic (machinery) (Laser Cut Machine Operator - "Lasercut machine operator")
WHERE TRIM(LOWER(co.migration_occupation)) = 'lasercut machine operator'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8544
);

UPDATE candidate_occupation co
SET occupation_id = 8686 -- Psychologist (Psychosocial Officer)
WHERE TRIM(LOWER(co.migration_occupation)) = 'psychosocial officer'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8686
);

UPDATE candidate_occupation co
SET occupation_id = 8700 -- Researcher (Research)
WHERE TRIM(LOWER(co.migration_occupation)) = 'research'
  AND co.occupation_id = 0
  AND NOT EXISTS (
    SELECT 1
    FROM candidate_occupation co2
    WHERE co2.candidate_id = co.candidate_id
      AND co2.occupation_id = 8700
);

-- unclear or duplicate data
DELETE FROM candidate_job_experience
WHERE candidate_occupation_id =  (SELECT id FROM candidate_occupation
                                  WHERE TRIM(LOWER(migration_occupation)) = 'asdfghjkl'
                                    AND occupation_id = 0
                                    AND candidate_id IN (
                                      SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                  ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'asdfghjkl'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = 'test'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'test'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);


DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = 'لايوجد'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'لايوجد'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = 'orv'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'orv'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = 'no'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'no'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = 'كل شي'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'كل شي'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = '....'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = '....'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id =  (SELECT id FROM candidate_occupation
                                  WHERE TRIM(LOWER(migration_occupation)) = 'there is no'
                                    AND occupation_id = 0
                                    AND candidate_id IN (
                                      SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                  ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'there is no'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);

DELETE FROM candidate_job_experience
WHERE candidate_occupation_id = (SELECT id FROM candidate_occupation
                                 WHERE TRIM(LOWER(migration_occupation)) = 'lebanon'
                                   AND occupation_id = 0
                                   AND candidate_id IN (
                                     SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
                                 ));
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'lebanon'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 0
);
-- Already have 8643 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 20086 AND occupation_id = 8643
    LIMIT 1
)
WHERE candidate_occupation_id = 20065;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'designing'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8643
);
-- Already have 8703 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 20534 AND occupation_id = 8703
    LIMIT 1
)
WHERE candidate_occupation_id = 20324;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'intern'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8703
);
-- Already have 8646 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 20718 AND occupation_id = 8646
    LIMIT 1
)
WHERE candidate_occupation_id = 20460;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'عيادتي الخاصة'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8646
);
-- Already have 8567 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 20725 AND occupation_id = 8567
    LIMIT 1
)
WHERE candidate_occupation_id = 20466;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'youth club'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8567
);
-- Already have 8614 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 20766 AND occupation_id = 8614
    LIMIT 1
)
WHERE candidate_occupation_id = 20490;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'صناعة الحلويات'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8614
);
-- Already have 8705 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 21139 AND occupation_id = 8705
    LIMIT 1
)
WHERE candidate_occupation_id = 20713;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'مدرب محادثة وتاسيس للغة الانكليزية'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8705
);
-- Already have 8712 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 22962 AND occupation_id = 8712
    LIMIT 1
)
WHERE candidate_occupation_id = 21683;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'media'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8712
);
-- Already have 8705 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 26166 AND occupation_id = 8705
    LIMIT 1
)
WHERE candidate_occupation_id = 22999;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'معلم صف رابع ابتدائي'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8705
);
-- Already have 8484 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 26508 AND occupation_id = 8484
    LIMIT 1
)
WHERE candidate_occupation_id = 23287;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'operation officer'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8484
);

-- Already have 8567 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 26590 AND occupation_id = 8567
    LIMIT 1
)
WHERE candidate_occupation_id = 23338;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'social worker'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8567
);

-- Already have 8567 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 26801 AND occupation_id = 8567
    LIMIT 1
)
WHERE candidate_occupation_id = 23507;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'non- profit ngo'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8567
);

-- Already have 8535 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 26823 AND occupation_id = 8535
    LIMIT 1
)
WHERE candidate_occupation_id = 23536;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'it/accountant assistant'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8535
);
-- Already have 8628 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 26878 AND occupation_id = 8628
    LIMIT 1
)
WHERE candidate_occupation_id = 23582;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'engineer ( electrical & electronic )'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8628
);
-- Already have 8556 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 27162 AND occupation_id = 8556
    LIMIT 1
)
WHERE candidate_occupation_id = 23812;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'سباك'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8556
);
-- Already have 8646 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 27195 AND occupation_id = 8646
    LIMIT 1
)
WHERE candidate_occupation_id = 23840;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'طبيب مقيم'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8646
);
-- Already have 8535 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 28579 AND occupation_id = 8535
    LIMIT 1
)
WHERE candidate_occupation_id = 24836;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'telecommunication'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8535
);

-- Already have 8712 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 29176 AND occupation_id = 8712
    LIMIT 1
)
WHERE candidate_occupation_id = 25319;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'translator'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8712
);
-- Already have 8499 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 29188 AND occupation_id = 8499
    LIMIT 1
)
WHERE candidate_occupation_id = 25338;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'supply chain and logistics'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8499
);
-- Already have 8535 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 29198 AND occupation_id = 8535
    LIMIT 1
)
WHERE candidate_occupation_id = 25351;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'information management officer'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8535
);
-- Already have 8535 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 29465 AND occupation_id = 8535
    LIMIT 1
)
WHERE candidate_occupation_id = 25653;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'red team | vulnerability researching | attack simulation'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8535
);

-- Already have 8499 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 32501 AND occupation_id = 8499
    LIMIT 1
)
WHERE candidate_occupation_id = 28508;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'الأمر معقد'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8499
);
-- Already have 8484 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 33618 AND occupation_id = 8484
    LIMIT 1
)
WHERE candidate_occupation_id = 29488;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'receptionist'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8484
);
-- Already have 8499 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 33802 AND occupation_id = 8499
    LIMIT 1
)
WHERE candidate_occupation_id = 29672;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'strategic consultant'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8499
);

-- Already have 8484 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 35464 AND occupation_id = 8484
    LIMIT 1
)
WHERE candidate_occupation_id = 31151;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'purchasing assistant'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8484
);

-- Already have 8703 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 35632 AND occupation_id = 8703
    LIMIT 1
)
WHERE candidate_occupation_id = 31286;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'second year-mecanical engineering'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8703
);

-- Already have 8648 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 37510 AND occupation_id = 8648
    LIMIT 1
)
WHERE candidate_occupation_id = 32897;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'nursing instuctor'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8648
);

-- Already have 8612 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 38530 AND occupation_id = 8612
    LIMIT 1
)
WHERE candidate_occupation_id = 33761;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'شف'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8612
);

-- Already have 8527 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 42725 AND occupation_id = 8527
    LIMIT 1
)
WHERE candidate_occupation_id = 37279;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'عامل نظافة'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8527
);
-- Already have 8691 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 21088 AND occupation_id = 8691
    LIMIT 1
)
WHERE candidate_occupation_id = 20689;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'بائع'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8691
);

-- Already have 8571 in occupation table and it is connected with candidate_job_experience so I need to update that first then delete the duplicate value
UPDATE candidate_job_experience
SET candidate_occupation_id = (
    SELECT id FROM candidate_occupation
    WHERE candidate_id = 38305 AND occupation_id = 8571
    LIMIT 1
)
WHERE candidate_occupation_id = 33555;
DELETE FROM candidate_occupation
WHERE TRIM(LOWER(migration_occupation)) = 'مصمم ازياء'
  AND occupation_id = 0
  AND candidate_id IN (
    SELECT candidate_id FROM candidate_occupation WHERE occupation_id = 8571
);

ALTER TABLE candidate_occupation
    DROP COLUMN migration_occupation;
