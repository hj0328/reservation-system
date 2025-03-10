SET REFERENTIAL_INTEGRITY FALSE;

truncate table category;
truncate table member;
truncate table place;
truncate table product;
truncate table product_price;
truncate table product_seat_schedule;
truncate table reservation;
truncate table reservation_price;

SET REFERENTIAL_INTEGRITY TRUE;
