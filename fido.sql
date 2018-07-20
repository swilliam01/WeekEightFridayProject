-- create database fido;
use fido;

select * from user;

-- get list of pets associated with user
select a.id as petID, user_id, username, a.name, date_lost, description, status, image
from pet a inner join user b on a.user_id = b.id; 
