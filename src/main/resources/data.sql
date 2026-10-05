-- Seed Data for Cloud-Based Library Management System

-- Books (6 records across categories: Computer Science, Software Engineering, DevOps, Cloud Computing, Database Systems)
INSERT INTO books (id, title, author, isbn, category, total_quantity, available_quantity, cover_image_url, created_at, updated_at)
VALUES 
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '978-0132350884', 'Software Engineering', 5, 3, 'https://images.example.com/clean-code.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'DevOps Handbook: How to Create World-Class Agility', 'Gene Kim, Jez Humble, Patrick Debois', '978-1942788003', 'DevOps', 4, 3, 'https://images.example.com/devops-handbook.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'AWS Certified Solutions Architect Official Study Guide', 'Joe Baron, Hisham Achenaki', '978-1119138556', 'Cloud Computing', 6, 5, 'https://images.example.com/aws-solutions-architect.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Database System Concepts', 'Abraham Silberschatz, Henry F. Korth', '978-0078022159', 'Database Systems', 3, 2, 'https://images.example.com/database-concepts.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Introduction to Algorithms', 'Thomas H. Cormen, Charles E. Leiserson', '978-0262033848', 'Computer Science', 4, 4, 'https://images.example.com/intro-algorithms.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Cloud Native Patterns: Designing Change-tolerant Software', 'Cornelia Davis', '978-1617295140', 'Cloud Computing', 5, 4, 'https://images.example.com/cloud-native.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Members (4 records: Librarians and Students with MEM001, MEM002, MEM003, MEM004)
INSERT INTO members (id, member_code, name, email, phone, role, status, registered_at)
VALUES 
(1, 'MEM001', 'Abraham Grace F', 'abraham.grace@library.com', '+1-555-0101', 'LIBRARIAN', 'ACTIVE', CURRENT_TIMESTAMP),
(2, 'MEM002', 'John Doe', 'john.doe@student.edu', '+1-555-0102', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
(3, 'MEM003', 'Jane Smith', 'jane.smith@student.edu', '+1-555-0103', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
(4, 'MEM004', 'Robert Johnson', 'robert.j@student.edu', '+1-555-0104', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP);

-- Borrow Records (3 records: Active issue, overdue issue, returned issue)
INSERT INTO borrow_records (id, member_id, book_id, issue_date, due_date, return_date, status, fine_amount)
VALUES 
(1, 2, 1, '2026-03-25', '2026-04-08', NULL, 'ISSUED', 0.00),
(2, 3, 2, '2026-03-01', '2026-03-15', NULL, 'OVERDUE', 15.50),
(3, 4, 4, '2026-02-15', '2026-03-01', '2026-03-03', 'RETURNED', 5.00);

-- Fines (2 records: Unpaid fine for overdue book, paid fine)
INSERT INTO fines (id, member_id, borrow_record_id, amount, status, paid_at, reason)
VALUES 
(1, 3, 2, 15.50, 'UNPAID', NULL, 'Overdue book return by 11 days'),
(2, 4, 3, 5.00, 'PAID', '2026-03-03 14:30:00', 'Overdue book return by 2 days');

-- Restart auto-increment identity sequences for H2/PostgreSQL after manual ID insertions
ALTER TABLE books ALTER COLUMN id RESTART WITH 100;
ALTER TABLE members ALTER COLUMN id RESTART WITH 100;
ALTER TABLE borrow_records ALTER COLUMN id RESTART WITH 100;
ALTER TABLE fines ALTER COLUMN id RESTART WITH 100;

