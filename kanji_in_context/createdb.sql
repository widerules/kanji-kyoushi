drop table if exists sentence cascade;
drop table if exists question cascade;
drop table if exists kinc_user cascade;
drop table if exists user_scores cascade;

create table sentence (
    sentence_id serial primary key,
    sentence_text text not null,
    workbook integer,
    lesson integer
);

create table question (
    question_id serial primary key,
    sentence_id integer 
        references sentence (sentence_id)
        on update cascade on delete cascade,
    question character varying(256) not null,
    answer character varying(256) not null
);

create table kinc_user (
    user_id serial primary key,
    user_name character varying(256) unique
);

create table user_scores (
    user_id integer 
        references kinc_user (user_id) 
        on update cascade on delete cascade,
    question_id integer 
        references question (question_id)
        on update cascade on delete cascade,
    score integer,
    
    primary key (user_id, question_id)
);
