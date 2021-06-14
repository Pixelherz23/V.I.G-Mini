/*==============================================================*/
/* DBMS name:      MySQL 5.0                                    */
/* Created on:     28.05.2021 14:38:31                          */
/*==============================================================*/

USE VIGMini; 

alter table GREENHOUSE 
   drop foreign key FK_GREENHOU_USER_GREE_USER;

alter table INFORMATION 
	drop check CHK_INFO;

alter table MEASUREMENTS 
   drop foreign key FK_MEASUREM_GREENHOUS_GREENHOU;

alter table SETTINGS 
   drop foreign key FK_SETTINGS_GREENHOUS_GREENHOU;

alter table TIMETABLE 
   drop foreign key FK_TIMETABL_SETTINGS__SETTINGS;
      
alter table TIMETABLE
	drop check CHK_TIMETABLE;

drop table if exists SETTINGS;

drop table if exists MEASUREMENTS;
   
drop table if exists INFORMATION;
   
drop table if exists GREENHOUSE;

drop table if exists TIMETABLE;

drop table if exists TBL_USER;

drop procedure if exists ACTIVATE_GREENHOUSE;

drop procedure if exists CHANGE_GREENHOUSE_NAME;

drop procedure if exists NEW_INFORMATION;

drop procedure if exists NEW_MEASUREMENTS;

drop procedure if exists UPDATE_SETTINGS;

drop procedure if exists NEW_USER;

drop procedure if exists UPDATE_USER;

drop view if exists NEWS;

drop view if exists HELP;

drop view if exists MEASUREMENTS_NOW;

drop view if exists MEASUREMENTS_DAY;

drop view if exists MEASUREMENTS_WEEK;

drop view if exists MEASUREMENTS_MONTH;

/*==============================================================*/
/* Table: GREENHOUSE                                            */
/*==============================================================*/
create table GREENHOUSE
(
   PRODUCT_KEY          varchar(15) not null  comment '',
   EMAIL                varchar(100)  comment '',
   NAME                 varchar(50) default 'Gewächshaus'  comment '',
   primary key (PRODUCT_KEY)
);

/*==============================================================*/
/* Table: INFORMATION                                           */
/*==============================================================*/
create table INFORMATION
(
   ID_IMFORMATION       int not null AUTO_INCREMENT comment '',
   INFORMATION_TYPE     varchar(10) not null  comment '',
   HEADLINE             varchar(255) not null  comment '',
   CONTENT              text not null  comment '',
   constraint CHK_INFO check (INFORMATION_TYPE='news' or INFORMATION_TYPE='help'),
   primary key (ID_IMFORMATION)
);

/*==============================================================*/
/* Table: MEASUREMENTS                                          */
/*==============================================================*/
create table MEASUREMENTS
(
   ID_MEASUREMENTS      bigint not null AUTO_INCREMENT  comment '',
   PRODUCT_KEY          varchar(15) not null  comment '',
   LED_STATUS           bool  comment '',
   TEMPERATURE          decimal(3,2)  comment '',
   HUMIDITY             int  comment '',
   SOIL_MOISTURE        decimal(3,2)  comment '',
   TIME_STAMP           timestamp default CURRENT_TIMESTAMP()  comment '',
   primary key (ID_MEASUREMENTS)
);

/*==============================================================*/
/* Table: SETTINGS                                              */
/*==============================================================*/
create table SETTINGS
(
   ID_SETTINGS          int not null AUTO_INCREMENT comment '',
   PRODUCT_KEY          varchar(15) not null  comment '',
   MAX_TEMPERATURE      smallint default 35 comment '',
   MIN_SOIL_MOISTURE    smallint default 10 comment '',
   TEMP_ON              bool default false comment '',
   SOIL_MOISTURE_ON     bool default false comment '',
   primary key (ID_SETTINGS)
);

/*==============================================================*/
/* Table: TIMETABLE                                             */
/*==============================================================*/
create table TIMETABLE
(
   ID_TIMETABLE         int not null AUTO_INCREMENT comment '',
   ID_SETTINGS          int not null  comment '',
   INTERVAL_TIME 		smallint not null comment '',
   TIMETABLE_TYPE       varchar(15) not null  comment '',
   FROM_TIME            time not null default '00:00:00'  comment '',
   TO_TIME              time  comment '',
   TIMETABLE_ON         bool not null default 0  comment '',
   constraint CHK_TIMETABLE check (TIMETABLE_TYPE='light' or TIMETABLE_TYPE='soilMoisture'),
   primary key (ID_TIMETABLE)
);

/*==============================================================*/
/* Table: TBL_USER                                              */
/*==============================================================*/
create table TBL_USER
(
   EMAIL                varchar(100) not null  comment '',
   LAST_NAME            varchar(50) not null  comment '',
   FIRST_NAME           varchar(50) not null  comment '',
   PASSWORD             VARCHAR(255) not null  comment '',
   primary key (EMAIL)
);


alter table GREENHOUSE add constraint FK_GREENHOU_USER_GREE_USER foreign key (EMAIL)
      references TBL_USER (EMAIL) on delete restrict on update restrict;

alter table MEASUREMENTS add constraint FK_MEASUREM_GREENHOUS_GREENHOU foreign key (PRODUCT_KEY)
      references GREENHOUSE (PRODUCT_KEY) on delete restrict on update restrict;

alter table SETTINGS add constraint FK_SETTINGS_GREENHOUS_GREENHOU foreign key (PRODUCT_KEY)
      references GREENHOUSE (PRODUCT_KEY) on delete restrict on update restrict;

alter table TIMETABLE add constraint FK_TIMETABL_SETTINGS__SETTINGS foreign key (ID_SETTINGS)
    	references SETTINGS (ID_SETTINGS) on delete restrict on update restrict;

/*==============================================================*/
/* Procedure: ACTIVATE_GREENHOUSE                               */
/*==============================================================*/
DELIMITER $$
create procedure ACTIVATE_GREENHOUSE(
	in p_product_key varchar(15),
	in p_email varchar(100),
	out p1 varchar(50)
)
begin
	if ( select exists (select 1 from GREENHOUSE where PRODUCT_KEY = p_product_key) ) then

		if ( select exists (select 1 from TBL_USER where EMAIL = p_email) ) then
			update GREENHOUSE
			set EMAIL = p_email
			where PRODUCT_KEY = p_product_key;
			insert into SETTINGS(PRODUCT_KEY)
			values(p_product_key);
			
			select ID_SETTINGS from SETTINGS where PRODUCT_KEY = p_product_key limit 1 into p1;
			insert into TIMETABLE(ID_SETTINGS, INTERVAL_TIME,  TIMETABLE_TYPE)
			values
				(p1, 1, 'light'),
				(p1, 2, 'light'),
				(p1, 3, 'light'),
				(p1, 1, 'soilMoisture'),
				(p1, 2, 'soilMoisture'),
				(p1, 3, 'soilMoisture');
					
			set p1 := 'Success';
		else
			set p1 := 'NoEmail';
		end if;
	else
		set p1 := 'NoGreenhouse';
	end if;
end$$
DELIMITER ;

/*==============================================================*/
/* Procedure: CHANGE_GREENHOUSE_NAME                               */
/*==============================================================*/
DELIMITER $$
create procedure CHANGE_GREENHOUSE_NAME(
	in p2_product_key varchar(15),
	in p2_name varchar(50),
	out p2 varchar(50)
)
begin
	if ( select exists (select 1 from GREENHOUSE where PRODUCT_KEY = p2_product_key) ) then
		update GREENHOUSE
		set NAME = p2_name
		where PRODUCT_KEY = p2_product_key;
		set p2 := "Success";
	else
		set p2:= "Greenhouse does not exist";
	end if;
end$$
DELIMITER ;

/*==============================================================*/
/* Procedure: NEW_INFORMATION                                           */
/*==============================================================*/
DELIMITER $$
create procedure NEW_INFORMATION(
	in p_info_type varchar(10),
	in p_headline varchar(255),
	in p_content text,
	out p3 varchar(50)
)
begin
	if ( select exists (select 1 from INFORMATION where HEADLINE = p_headline) ) then
		set p3 := 'Information exist';
		
	else
		insert into INFORMATION(INFORMATION_TYPE, HEADLINE, CONTENT)
		values(p_info_type, p_headline, p_content);
		set p3 := "Success";
	end if;
end$$
DELIMITER ;

/*==============================================================*/
/* Procedure: NEW_MEASUREMENTS                                        */
/*==============================================================*/
DELIMITER $$
create procedure NEW_MEASUREMENTS(
	in p3_product_key varchar(15),
	in p_led_status bool,
	in p_temperature decimal(3,2),
	in p_humidity int,
	in p_soil_moisture decimal(3,2),
	out p4 varchar(50)
)
begin
	if ( select exists (select 1 from GREENHOUSE where PRODUCT_KEY = p3_product_key) ) then
		insert into MEASUREMENTS(PRODUCT_KEY, LED_STATUS, TEMPERATURE, HUMIDITY, SOIL_MOISTURE)
		values(p3_product_key, p_led_status, p_temperature, p_humidity, p_soil_moisture);
		set p4 := "Success";
	else
		set p4:= "Greenhouse does not exist";
	end if;
end$$
DELIMITER ;

/*==============================================================*/
/* Procedure: UPDATE_SETTINGS                                              */
/*==============================================================*/
DELIMITER $$
create procedure UPDATE_SETTINGS(
	in p4_product_key varchar(15),
	in p_max_temperature smallint,
	in p_min_soil_moisture smallint,
	in p_temp_on  bool,
	in p_soil_moisture_on bool,
	in p_timetable_type varchar(15),
	in p_interval smallint,
	in p_from time,
	in p_to time,
	in p_timetable_on bool,
	out p5 varchar(50)
	
)
begin
	if ( select exists (select 1 from GREENHOUSE where PRODUCT_KEY = p4_product_key) ) then
		if(p_max_temperature is NOT NULL) then 
			update SETTINGS set MAX_TEMPERATURE = p_max_temperature where PRODUCT_KEY = p4_product_key; end if;
		
		if(p_min_soil_moisture is NOT NULL) then 
			update SETTINGS set MIN_SOIL_MOISTURE = p_min_soil_moisture where PRODUCT_KEY = p4_product_key; end if;
		
		if(p_temp_on is NOT NULL) then 
			update SETTINGS set TEMP_ON = p_temp_on where PRODUCT_KEY = p4_product_key; end if;
			
		if(p_soil_moisture_on is NOT NULL) then 
			update SETTINGS set SOIL_MOISTURE_ON = p_soil_moisture_on where PRODUCT_KEY = p4_product_key; end if;
			
		select ID_SETTINGS from SETTINGS where PRODUCT_KEY = p4_product_key limit 1 into p5;
		
		if(p_timetable_type is NOT NULL and p_interval is NOT NULL) then
			if(p_from is NOT NULL) then
				update TIMETABLE set FROM_TIME = p_from 
				where ID_SETTINGS = p5 and TIMETABLE_TYPE = p_timetable_type and INTERVAL_TIME = p_interval; end if;
			
			if(p_to is NOT NULL) then
				update TIMETABLE set TO_TIME = p_to 
				where ID_SETTINGS = p5 and TIMETABLE_TYPE = p_timetable_type and INTERVAL_TIME = p_interval; end if;
				
			if(p_timetable_on is NOT NULL) then
				update TIMETABLE set TIMETABLE_ON = p_timetable_on 
				where ID_SETTINGS = p5 and TIMETABLE_TYPE = p_timetable_type and INTERVAL_TIME = p_interval; end if;
			end if;
			
		set p5 := "Success";
	else
		set p5:= "Greenhouse does not exist";
	end if;
end$$
DELIMITER ;

/*==============================================================*/
/* Procedure: NEW_USER                                          */
/*==============================================================*/

DELIMITER $$

create procedure NEW_USER(
	in p_first_name varchar(50),
	in p_last_name varchar(50),
	in p_email varchar(100),
	in p_password VARCHAR(255),
	out p6 varchar(50)
)
begin
	if ( select exists (select 1 from TBL_USER where EMAIL = p_email) ) then
		set p6 := "User exist";
	
	else
		insert into TBL_USER(EMAIL, LAST_NAME, FIRST_NAME, PASSWORD)
		values(p_email, p_last_name, p_first_name, p_password);
		set p6 := "Success";
	end if;
end$$
DELIMITER ;

/*==============================================================*/
/* Procedure: UPDATE_USER                                       */
/*==============================================================*/

DELIMITER $$

create procedure UPDATE_USER(
	in p2_first_name varchar(50),
	in p2_last_name varchar(50),
	in p2_email varchar(100),
	in p_new_email varchar(100),
	in p2_password VARCHAR(255),
	out p7 varchar(50)
)
begin 
	if ( select exists (select 1 from TBL_USER where EMAIL = p2_email) ) then
		if (p_new_email is NULL) then
			update TBL_USER set FIRST_NAME = p2_first_name, LAST_NAME = p2_last_name
			where EMAIL = p2_email;
			if (p2_password is NOT NULL) then
				update TBL_USER set PASSWORD = p2_password 
				where EMAIL = p2_email; end if;
			set p7 := 'Success';  
			
		else
			if( select exists (select 1 from GREENHOUSE where EMAIL = p2_email)) then
				select PRODUCT_KEY from GREENHOUSE where EMAIL = p2_email limit 1 into p7;
				update GREENHOUSE set EMAIL = NULL where EMAIL = p2_email;
				
				update TBL_USER 
				set FIRST_NAME = p2_first_name, LAST_NAME = p2_last_name, EMAIL = p_new_email
				where EMAIL = p2_email;
				if (p2_password is NOT NULL) then
					update TBL_USER set PASSWORD = p2_password 
					where EMAIL = p2_email; end if;
				
				update GREENHOUSE set EMAIL = p_new_email where PRODUCT_KEY = p7;
				
			else
				
				update TBL_USER 
				set FIRST_NAME = p2_first_name, LAST_NAME = p2_last_name, EMAIL = p_new_email
				where EMAIL = p2_email; 

				if (p2_password is NOT NULL) then
					update TBL_USER set PASSWORD = p2_password 
					where EMAIL = p_new_email; end if;
				end if;
			set p7 := 'Success';
		end if;
		
	else
		set p7 := 'User does not exist';
	end if;
end$$
DELIMITER ;			


create view NEWS as
	select HEADLINE, CONTENT
	from INFORMATION
	where INFORMATION_TYPE = 'news';
	
create view HELP as
	select HEADLINE, CONTENT
	from INFORMATION
	where INFORMATION_TYPE = 'help';
	
create view MEASUREMENTS_NOW as
	select PRODUCT_KEY, TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP
	from MEASUREMENTS
	order by TIME_STAMP desc
	limit 1;
	
create view MEASUREMENTS_DAY as
	select PRODUCT_KEY, TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP
	from MEASUREMENTS
	where timestampdiff(HOUR, TIME_STAMP, CURRENT_TIMESTAMP()) <= 24;

create view MEASUREMENTS_WEEK as
	select PRODUCT_KEY, TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP
	from MEASUREMENTS
	where timestampdiff(DAY, TIME_STAMP, CURRENT_TIMESTAMP()) <= 7;	
	
create view MEASUREMENTS_MONTH as
	select PRODUCT_KEY, TEMPERATURE, HUMIDITY, SOIL_MOISTURE, TIME_STAMP
	from MEASUREMENTS
	where timestampdiff(WEEK, TIME_STAMP, CURRENT_TIMESTAMP()) <= 4;
	

	
