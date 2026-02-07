-- V14: admin@petpro.com 계정을 SUPER_ADMIN으로 승격
UPDATE users SET role = 'SUPER_ADMIN' WHERE email = 'admin@petpro.com';
