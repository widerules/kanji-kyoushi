-- You can use this file to load seed data into the database using SQL statements
--insert into Member (id, name, email, phone_number) values (0, 'John Smith', 'john.smith@mailinator.com', '2125551212')

insert into vocabularyword (id, word) values (0, '勉強');
insert into vocabularyword_readings (vocabularyword_id, readings) values (0, 'べんきょう');
insert into vocabularyword_meanings (vocabularyword_id, meanings) values (0, 'study');

insert into vocabularyword (id, word) values (1, '台風');
insert into vocabularyword_readings (vocabularyword_id, readings) values (1, 'たいふう');
insert into vocabularyword_meanings (vocabularyword_id, meanings) values (1, 'typhoon');

insert into vocabularyword (id, word) values (2, '分析');
insert into vocabularyword_readings (vocabularyword_id, readings) values (2, 'ぶんせき');
insert into vocabularyword_meanings (vocabularyword_id, meanings) values (2, 'analysis');

insert into vocabularyword (id, word) values (3, '開発');
insert into vocabularyword_readings (vocabularyword_id, readings) values (3, 'かいはつ');
insert into vocabularyword_meanings (vocabularyword_id, meanings) values (3, 'develop');

insert into vocabularyword (id, word) values (4, '四');
insert into vocabularyword_readings (vocabularyword_id, readings) values (4, 'よん');
insert into vocabularyword_readings (vocabularyword_id, readings) values (4, 'し');
insert into vocabularyword_meanings (vocabularyword_id, meanings) values (4, '4');

