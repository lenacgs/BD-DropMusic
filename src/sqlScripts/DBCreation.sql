CREATE TABLE user (
	username varchar(512) NOT NULL UNIQUE,
	password varchar(512) NOT NULL,
	editor	 boolean NOT NULL,
	PRIMARY KEY(username)
);

CREATE TABLE playlist (
	name		 varchar(256) NOT NULL UNIQUE,
	private	 boolean NOT NULL,
	user_username varchar(256),
	PRIMARY KEY(name,user_username)
);

CREATE TABLE review (
	rate		 int NOT NULL,
	text		 text NOT NULL,
	album_id	 int NOT NULL UNIQUE,
	user_username varchar(512),
	PRIMARY KEY(album_id,user_username)
);

CREATE TABLE musicfile (
	pathtofile	 varchar(512) UNIQUE,
	music_id	 int NOT NULL UNIQUE,
	user_username varchar(512) NOT NULL,
	PRIMARY KEY(pathtofile)
);

CREATE TABLE music (
	id	 int AUTO_INCREMENT UNIQUE NOT NULL,
	title	 varchar(512) NOT NULL,
	duration float(8) NOT NULL,
	lyrics	 text,
	PRIMARY KEY(id)
);

CREATE TABLE concert (
	datetime		 timestamp,
	venue_name		 varchar(512) NOT NULL,
	venue_local_country varchar(512) NOT NULL,
	venue_local_city	 varchar(512) NOT NULL,
	PRIMARY KEY(datetime)
);

CREATE TABLE interpreter (
	id		 int AUTO_INCREMENT NOT NULL UNIQUE,
	name	 varchar(512) NOT NULL,
	description text NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE band (
	dateofcreation int,
	interpreter_id int NOT NULL UNIQUE,
	PRIMARY KEY(interpreter_id)
);

CREATE TABLE artist (
	birthday	 timestamp,
	interpreter_id int NOT NULL UNIQUE,
	PRIMARY KEY(interpreter_id)
);

CREATE TABLE album (
	id		 int AUTO_INCREMENT NOT NULL UNIQUE,
	title		 varchar(512) NOT NULL,
	yearofpublication int NOT NULL,
	description	 text NOT NULL,
	genre		 varchar(512) NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE composer (
	id		 int AUTO_INCREMENT NOT NULL UNIQUE,
	name	 varchar(512) NOT NULL,
	description text NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE publisher (
	name varchar(448) unique NOT NULL,
	PRIMARY KEY(name)
);

CREATE TABLE local (
	country varchar(256) unique NOT NULL,
	city	varchar(256) unique NOT NULL,
	PRIMARY KEY(country,city)
);

CREATE TABLE venue (
	name		 varchar(256) NOT NULL,
	local_country varchar(256) unique NOT NULL,
	local_city	 varchar(256) unique NOT NULL, 
	PRIMARY KEY(name,local_country,local_city)
);

CREATE TABLE user_musicfile (
	user_username	 varchar(256) NOT NULL,
	musicfile_pathtofile varchar(256) not null,
	PRIMARY KEY(user_username,musicfile_pathtofile)
);

CREATE TABLE interpreter_concert (
	interpreter_id	 int not null,
	concert_datetime timestamp not null,
	PRIMARY KEY(interpreter_id,concert_datetime)
);

CREATE TABLE band_artist (
	band_interpreter_id	 int not null,
	artist_interpreter_id int not null,
	PRIMARY KEY(band_interpreter_id,artist_interpreter_id)
);

CREATE TABLE music_interpreter (
	music_id	 int not null,
	interpreter_id int not null,
	PRIMARY KEY(music_id,interpreter_id)
);

CREATE TABLE album_publisher (
	album_id	 int not null,
	publisher_name varchar(448) not null,
	PRIMARY KEY(album_id,publisher_name)
);

CREATE TABLE artist_composer (
	artist_interpreter_id int not null,
	composer_id		 int not null,
	PRIMARY KEY(artist_interpreter_id)
);

CREATE TABLE composer_music (
	composer_id int not null,
	music_id	 int not null,
	PRIMARY KEY(composer_id,music_id)
);

CREATE TABLE album_music (
	album_id int not null,
	music_id int not null,
	PRIMARY KEY(album_id,music_id)
);

CREATE TABLE playlist_music (
	playlist_name		 varchar(256) not null,
	playlist_user_username varchar(256) not null,
	music_id		 int not null,
	PRIMARY KEY(playlist_name,playlist_user_username,music_id)
);


# Foreign keys -----------------------------

ALTER TABLE playlist ADD CONSTRAINT playlist_fk1 FOREIGN KEY (user_username) REFERENCES user(username);
ALTER TABLE review ADD CONSTRAINT review_fk1 FOREIGN KEY (album_id) REFERENCES album(id);
ALTER TABLE review ADD CONSTRAINT review_fk2 FOREIGN KEY (user_username) REFERENCES user(username);
ALTER TABLE musicfile ADD CONSTRAINT musicfile_fk1 FOREIGN KEY (music_id) REFERENCES music(id);
ALTER TABLE musicfile ADD CONSTRAINT musicfile_fk2 FOREIGN KEY (user_username) REFERENCES user(username);
ALTER TABLE band ADD CONSTRAINT band_fk1 FOREIGN KEY (interpreter_id) REFERENCES interpreter(id);
ALTER TABLE artist ADD CONSTRAINT artist_fk1 FOREIGN KEY (interpreter_id) REFERENCES interpreter(id);
ALTER TABLE user_musicfile ADD CONSTRAINT user_musicfile_fk1 FOREIGN KEY (user_username) REFERENCES user(username);
ALTER TABLE user_musicfile ADD CONSTRAINT user_musicfile_fk2 FOREIGN KEY (musicfile_pathtofile) REFERENCES musicfile(pathtofile);
ALTER TABLE interpreter_concert ADD CONSTRAINT interpreter_concert_fk1 FOREIGN KEY (interpreter_id) REFERENCES interpreter(id);
ALTER TABLE interpreter_concert ADD CONSTRAINT interpreter_concert_fk2 FOREIGN KEY (concert_datetime) REFERENCES concert(datetime);
ALTER TABLE band_artist ADD CONSTRAINT band_artist_fk1 FOREIGN KEY (band_interpreter_id) REFERENCES band(interpreter_id);
ALTER TABLE band_artist ADD CONSTRAINT band_artist_fk2 FOREIGN KEY (artist_interpreter_id) REFERENCES artist(interpreter_id);
ALTER TABLE music_interpreter ADD CONSTRAINT music_interpreter_fk1 FOREIGN KEY (music_id) REFERENCES music(id);
ALTER TABLE music_interpreter ADD CONSTRAINT music_interpreter_fk2 FOREIGN KEY (interpreter_id) REFERENCES interpreter(id);
ALTER TABLE album_publisher ADD CONSTRAINT album_publisher_fk1 FOREIGN KEY (album_id) REFERENCES album(id);
ALTER TABLE album_publisher ADD CONSTRAINT album_publisher_fk2 FOREIGN KEY (publisher_name) REFERENCES publisher(name);
ALTER TABLE artist_composer ADD CONSTRAINT artist_composer_fk1 FOREIGN KEY (artist_interpreter_id) REFERENCES artist(interpreter_id);
ALTER TABLE artist_composer ADD CONSTRAINT artist_composer_fk2 FOREIGN KEY (composer_id) REFERENCES composer(id);
ALTER TABLE composer_music ADD CONSTRAINT composer_music_fk1 FOREIGN KEY (composer_id) REFERENCES composer(id);
ALTER TABLE composer_music ADD CONSTRAINT composer_music_fk2 FOREIGN KEY (music_id) REFERENCES music(id);
ALTER TABLE album_music ADD CONSTRAINT album_music_fk1 FOREIGN KEY (album_id) REFERENCES album(id);
ALTER TABLE album_music ADD CONSTRAINT album_music_fk2 FOREIGN KEY (music_id) REFERENCES music(id);
ALTER TABLE playlist_music ADD CONSTRAINT playlist_music_fk1 FOREIGN KEY (playlist_name) REFERENCES playlist(name);
ALTER TABLE playlist_music ADD CONSTRAINT playlist_music_fk2 FOREIGN KEY (playlist_user_username) REFERENCES playlist(user_username);
ALTER TABLE playlist_music ADD CONSTRAINT playlist_music_fk3 FOREIGN KEY (music_id) REFERENCES music(id);
ALTER TABLE venue ADD CONSTRAINT venue_fk1 FOREIGN KEY (local_country) REFERENCES local(country);
ALTER TABLE concert ADD CONSTRAINT concert_fk1 FOREIGN KEY (venue_name) REFERENCES venue(name);
ALTER TABLE concert ADD CONSTRAINT concert_fk2 FOREIGN KEY (venue_local_country) REFERENCES venue(local_country);
ALTER TABLE venue ADD CONSTRAINT venue_fk2 FOREIGN KEY (local_city) REFERENCES local(city);
ALTER TABLE concert ADD CONSTRAINT concert_fk3 FOREIGN KEY (venue_local_city) REFERENCES venue(local_city);