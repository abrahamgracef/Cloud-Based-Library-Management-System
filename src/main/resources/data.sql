-- Seed Data for Cloud-Based Library Management System (LibraCore)

-- Books Catalog (22 Real Books with High-Quality Covers across CS, DevOps, Cloud, DB, AI, Cybersecurity)
INSERT INTO books (id, title, author, isbn, category, total_quantity, available_quantity, cover_image_url, created_at, updated_at)
VALUES 
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', '978-0132350884', 'Software Engineering', 6, 6, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'The DevOps Handbook: How to Create World-Class Agility', 'Gene Kim, Jez Humble, Patrick Debois', '978-1942788003', 'DevOps', 5, 5, 'https://images.unsplash.com/photo-1618401471353-b98afee0b2eb?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'AWS Certified Solutions Architect Official Study Guide', 'Joe Baron, Hisham Achenaki', '978-1119138556', 'Cloud Computing', 7, 7, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Database System Concepts (7th Edition)', 'Abraham Silberschatz, Henry F. Korth', '978-0078022159', 'Database Systems', 4, 4, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Introduction to Algorithms (4th Edition)', 'Thomas H. Cormen, Charles E. Leiserson', '978-0262033848', 'Computer Science', 5, 5, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Cloud Native Patterns: Designing Change-tolerant Software', 'Cornelia Davis', '978-1617295140', 'Cloud Computing', 5, 5, 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Designing Data-Intensive Applications', 'Martin Kleppmann', '978-1449373320', 'Software Engineering', 6, 6, 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Artificial Intelligence: A Modern Approach', 'Stuart Russell, Peter Norvig', '978-0134610993', 'Artificial Intelligence', 4, 4, 'https://images.unsplash.com/photo-1677442136019-21780efad99a?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Site Reliability Engineering: How Google Runs Production Systems', 'Betsy Beyer, Chris Jones', '978-1491929124', 'DevOps', 5, 5, 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Head First Design Patterns', 'Eric Freeman, Elisabeth Robson', '978-0596007126', 'Software Engineering', 4, 4, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Modern Operating Systems', 'Andrew S. Tanenbaum', '978-0133591620', 'Computer Science', 4, 4, 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Kubernetes in Action', 'Marko Luksa', '978-1617293726', 'DevOps', 5, 5, 'https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Learning Python (5th Edition)', 'Mark Lutz', '978-1449355739', 'Programming', 6, 6, 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Deep Learning', 'Ian Goodfellow, Yoshua Bengio', '978-0262035613', 'Artificial Intelligence', 3, 3, 'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'The Pragmatic Programmer', 'David Thomas, Andrew Hunt', '978-0135957059', 'Software Engineering', 5, 5, 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'System Design Interview – An Insider''s Guide', 'Alex Xu', '978-1736049112', 'Software Engineering', 6, 6, 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Computer Networking: A Top-Down Approach', 'James Kurose, Keith Ross', '978-0133594140', 'Computer Science', 4, 4, 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Continuous Delivery: Reliable Software Releases', 'Jez Humble, David Farley', '978-0321601910', 'DevOps', 5, 5, 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'PostgreSQL: Up and Running', 'Regina Obe, Leo Hsu', '978-1491903414', 'Database Systems', 4, 4, 'https://images.unsplash.com/photo-1507842217343-583bb7270b66?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Web Development with Node and Express', 'Ethan Brown', '978-1491949306', 'Web Development', 5, 5, 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Cybersecurity Essentials', 'Charles J. Brooks', '978-1119362395', 'Cybersecurity', 4, 4, 'https://images.unsplash.com/photo-1563986768609-322da13575f3?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Data Structures and Algorithms in Java', 'Robert Lafore', '978-0672324536', 'Computer Science', 5, 5, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Members (Librarians and Students)
INSERT INTO members (id, member_code, name, email, phone, role, status, registered_at)
VALUES 
(1, 'MEM001', 'Abraham Grace F', 'abraham.grace@library.com', '+91-9876543210', 'LIBRARIAN', 'ACTIVE', CURRENT_TIMESTAMP),
(2, 'MEM002', 'John Doe', 'john.doe@student.vit.ac.in', '+91-9876543211', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
(3, 'MEM003', 'Jane Smith', 'jane.smith@student.vit.ac.in', '+91-9876543212', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
(4, 'MEM004', 'Robert Johnson', 'robert.j@student.vit.ac.in', '+91-9876543213', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
(5, 'MEM005', 'Rahul Sharma', 'rahul.sharma@student.vit.ac.in', '+91-9876543214', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP),
(6, 'MEM006', 'Priya Nair', 'priya.nair@student.vit.ac.in', '+91-9876543215', 'STUDENT', 'ACTIVE', CURRENT_TIMESTAMP);

-- Sequence Restart
ALTER TABLE books ALTER COLUMN id RESTART WITH 100;
ALTER TABLE members ALTER COLUMN id RESTART WITH 100;
ALTER TABLE borrow_records ALTER COLUMN id RESTART WITH 100;
ALTER TABLE fines ALTER COLUMN id RESTART WITH 100;
