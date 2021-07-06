/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

insert into country (id, name, status) values (1000, 'Stateless', 'active');

create table country_nationality_join
(
    country_id bigint not null references country,
    nationality_id bigint not null references nationality,
    primary key (country_id, nationality_id)
);

insert into country_nationality_join (country_id, nationality_id) values (0, 0);
insert into country_nationality_join (country_id, nationality_id) values (6178, 9242);
insert into country_nationality_join (country_id, nationality_id) values (6179, 9289);
insert into country_nationality_join (country_id, nationality_id) values (6179, 9263);
insert into country_nationality_join (country_id, nationality_id) values (6179, 9347);
insert into country_nationality_join (country_id, nationality_id) values (6179, 9378);
insert into country_nationality_join (country_id, nationality_id) values (6179, 9414);
insert into country_nationality_join (country_id, nationality_id) values (6180, 9239);
insert into country_nationality_join (country_id, nationality_id) values (6181, 9240);
insert into country_nationality_join (country_id, nationality_id) values (6182, 9241);
insert into country_nationality_join (country_id, nationality_id) values (6184, 9243);
insert into country_nationality_join (country_id, nationality_id) values (6185, 9244);
insert into country_nationality_join (country_id, nationality_id) values (6188, 9245);
insert into country_nationality_join (country_id, nationality_id) values (6189, 9246);
insert into country_nationality_join (country_id, nationality_id) values (6190, 9247);
insert into country_nationality_join (country_id, nationality_id) values (6191, 9248);
insert into country_nationality_join (country_id, nationality_id) values (6192, 9249);
insert into country_nationality_join (country_id, nationality_id) values (6193, 9250);
insert into country_nationality_join (country_id, nationality_id) values (6194, 9251);
insert into country_nationality_join (country_id, nationality_id) values (6195, 9252);
insert into country_nationality_join (country_id, nationality_id) values (6196, 9253);
insert into country_nationality_join (country_id, nationality_id) values (6197, 9254);
insert into country_nationality_join (country_id, nationality_id) values (6198, 9255);
insert into country_nationality_join (country_id, nationality_id) values (6199, 9256);
insert into country_nationality_join (country_id, nationality_id) values (6200, 9257);
insert into country_nationality_join (country_id, nationality_id) values (6202, 9258);
insert into country_nationality_join (country_id, nationality_id) values (6205, 9260);
insert into country_nationality_join (country_id, nationality_id) values (6207, 9261);
insert into country_nationality_join (country_id, nationality_id) values (6211, 9264);
insert into country_nationality_join (country_id, nationality_id) values (6212, 9265);
insert into country_nationality_join (country_id, nationality_id) values (6213, 9266);
insert into country_nationality_join (country_id, nationality_id) values (6214, 9267);
insert into country_nationality_join (country_id, nationality_id) values (6215, 9268);
insert into country_nationality_join (country_id, nationality_id) values (6216, 9269);
insert into country_nationality_join (country_id, nationality_id) values (6217, 9271);
insert into country_nationality_join (country_id, nationality_id) values (6221, 9272);
insert into country_nationality_join (country_id, nationality_id) values (6222, 9273);
insert into country_nationality_join (country_id, nationality_id) values (6223, 9274);
insert into country_nationality_join (country_id, nationality_id) values (6226, 9275);
insert into country_nationality_join (country_id, nationality_id) values (6227, 9276);
insert into country_nationality_join (country_id, nationality_id) values (6233, 9278);
insert into country_nationality_join (country_id, nationality_id) values (6234, 9279);
insert into country_nationality_join (country_id, nationality_id) values (6236, 9280);
insert into country_nationality_join (country_id, nationality_id) values (6237, 9281);
insert into country_nationality_join (country_id, nationality_id) values (6238, 9282);
insert into country_nationality_join (country_id, nationality_id) values (6241, 9283);
insert into country_nationality_join (country_id, nationality_id) values (6242, 9286);
insert into country_nationality_join (country_id, nationality_id) values (6243, 9287);
insert into country_nationality_join (country_id, nationality_id) values (6244, 9375);
insert into country_nationality_join (country_id, nationality_id) values (6246, 9290);
insert into country_nationality_join (country_id, nationality_id) values (6247, 9291);
insert into country_nationality_join (country_id, nationality_id) values (6248, 9292);
insert into country_nationality_join (country_id, nationality_id) values (6250, 9293);
insert into country_nationality_join (country_id, nationality_id) values (6251, 9295);
insert into country_nationality_join (country_id, nationality_id) values (6252, 9294);
insert into country_nationality_join (country_id, nationality_id) values (6253, 9297);
insert into country_nationality_join (country_id, nationality_id) values (6254, 9305);
insert into country_nationality_join (country_id, nationality_id) values (6258, 9298);
insert into country_nationality_join (country_id, nationality_id) values (6259, 9299);
insert into country_nationality_join (country_id, nationality_id) values (6260, 9300);
insert into country_nationality_join (country_id, nationality_id) values (6262, 9302);
insert into country_nationality_join (country_id, nationality_id) values (6264, 9303);
insert into country_nationality_join (country_id, nationality_id) values (6267, 9304);
insert into country_nationality_join (country_id, nationality_id) values (6269, 9306);
insert into country_nationality_join (country_id, nationality_id) values (6270, 9307);
insert into country_nationality_join (country_id, nationality_id) values (6271, 9308);
insert into country_nationality_join (country_id, nationality_id) values (6272, 9309);
insert into country_nationality_join (country_id, nationality_id) values (6273, 9310);
insert into country_nationality_join (country_id, nationality_id) values (6274, 9311);
insert into country_nationality_join (country_id, nationality_id) values (6275, 9312);
insert into country_nationality_join (country_id, nationality_id) values (6276, 9313);
insert into country_nationality_join (country_id, nationality_id) values (6277, 9314);
insert into country_nationality_join (country_id, nationality_id) values (6278, 9315);
insert into country_nationality_join (country_id, nationality_id) values (6279, 9316);
insert into country_nationality_join (country_id, nationality_id) values (6280, 9317);

insert into country_nationality_join (country_id, nationality_id) values (6281, 9318);
insert into country_nationality_join (country_id, nationality_id) values (6283, 9319);
insert into country_nationality_join (country_id, nationality_id) values (6284, 9320);
insert into country_nationality_join (country_id, nationality_id) values (6285, 9322);
insert into country_nationality_join (country_id, nationality_id) values (6286, 9323);
insert into country_nationality_join (country_id, nationality_id) values (6288, 9324);
insert into country_nationality_join (country_id, nationality_id) values (6289, 9325);
insert into country_nationality_join (country_id, nationality_id) values (6290, 9326);
insert into country_nationality_join (country_id, nationality_id) values (6292, 9330);
insert into country_nationality_join (country_id, nationality_id) values (6293, 9331);
insert into country_nationality_join (country_id, nationality_id) values (6295, 9333);
insert into country_nationality_join (country_id, nationality_id) values (6296, 9334);
insert into country_nationality_join (country_id, nationality_id) values (6298, 9335);
insert into country_nationality_join (country_id, nationality_id) values (6299, 9336);
insert into country_nationality_join (country_id, nationality_id) values (6300, 9337);
insert into country_nationality_join (country_id, nationality_id) values (6301, 9338);
insert into country_nationality_join (country_id, nationality_id) values (6302, 9339);
insert into country_nationality_join (country_id, nationality_id) values (6306, 9343);
insert into country_nationality_join (country_id, nationality_id) values (6307, 9342);
insert into country_nationality_join (country_id, nationality_id) values (6308, 9344);
insert into country_nationality_join (country_id, nationality_id) values (6309, 9345);
insert into country_nationality_join (country_id, nationality_id) values (6310, 9346);
insert into country_nationality_join (country_id, nationality_id) values (6313, 9348);
insert into country_nationality_join (country_id, nationality_id) values (6316, 9349);
insert into country_nationality_join (country_id, nationality_id) values (6320, 9352);
insert into country_nationality_join (country_id, nationality_id) values (6321, 9353);
insert into country_nationality_join (country_id, nationality_id) values (6323, 9351);
insert into country_nationality_join (country_id, nationality_id) values (6326, 9354);
insert into country_nationality_join (country_id, nationality_id) values (6328, 9355);
insert into country_nationality_join (country_id, nationality_id) values (6329, 9284);
insert into country_nationality_join (country_id, nationality_id) values (6331, 9356);
insert into country_nationality_join (country_id, nationality_id) values (6332, 9357);
insert into country_nationality_join (country_id, nationality_id) values (6334, 9358);
insert into country_nationality_join (country_id, nationality_id) values (6339, 9359);
insert into country_nationality_join (country_id, nationality_id) values (6341, 9360);
insert into country_nationality_join (country_id, nationality_id) values (6342, 9361);
insert into country_nationality_join (country_id, nationality_id) values (6343, 9362);

insert into country_nationality_join (country_id, nationality_id) values (6344, 9363);
insert into country_nationality_join (country_id, nationality_id) values (6345, 9364);
insert into country_nationality_join (country_id, nationality_id) values (6346, 9365);
insert into country_nationality_join (country_id, nationality_id) values (6347, 9366);
insert into country_nationality_join (country_id, nationality_id) values (6348, 9296);
insert into country_nationality_join (country_id, nationality_id) values (6349, 9367);
insert into country_nationality_join (country_id, nationality_id) values (6350, 9368);
insert into country_nationality_join (country_id, nationality_id) values (6351, 9369);
insert into country_nationality_join (country_id, nationality_id) values (6354, 9372);
insert into country_nationality_join (country_id, nationality_id) values (6355, 9373);
insert into country_nationality_join (country_id, nationality_id) values (6356, 9374);
insert into country_nationality_join (country_id, nationality_id) values (6360, 9390);
insert into country_nationality_join (country_id, nationality_id) values (6366, 9376);
insert into country_nationality_join (country_id, nationality_id) values (6367, 9377);
insert into country_nationality_join (country_id, nationality_id) values (6368, 9379);
insert into country_nationality_join (country_id, nationality_id) values (6369, 9380);
insert into country_nationality_join (country_id, nationality_id) values (6371, 9381);
insert into country_nationality_join (country_id, nationality_id) values (6372, 9382);
insert into country_nationality_join (country_id, nationality_id) values (6375, 9385);
insert into country_nationality_join (country_id, nationality_id) values (6377, 9386);

insert into country_nationality_join (country_id, nationality_id) values (6378, 9387);
insert into country_nationality_join (country_id, nationality_id) values (6378, 9259);
insert into country_nationality_join (country_id, nationality_id) values (6379, 9327);
insert into country_nationality_join (country_id, nationality_id) values (6381, 9388);
insert into country_nationality_join (country_id, nationality_id) values (6381, 9270);
insert into country_nationality_join (country_id, nationality_id) values (6382, 9389);
insert into country_nationality_join (country_id, nationality_id) values (6383, 9391);

insert into country_nationality_join (country_id, nationality_id) values (6384, 9392);
insert into country_nationality_join (country_id, nationality_id) values (6387, 9393);
insert into country_nationality_join (country_id, nationality_id) values (6388, 9394);
insert into country_nationality_join (country_id, nationality_id) values (6389, 9396);

insert into country_nationality_join (country_id, nationality_id) values (6389, 9395);
insert into country_nationality_join (country_id, nationality_id) values (6390, 9397);
insert into country_nationality_join (country_id, nationality_id) values (6391, 9398);
insert into country_nationality_join (country_id, nationality_id) values (6392, 9399);
insert into country_nationality_join (country_id, nationality_id) values (6393, 9400);
insert into country_nationality_join (country_id, nationality_id) values (6398, 9403);
insert into country_nationality_join (country_id, nationality_id) values (6398, 9402);
insert into country_nationality_join (country_id, nationality_id) values (6399, 9404);
insert into country_nationality_join (country_id, nationality_id) values (6400, 9405);
insert into country_nationality_join (country_id, nationality_id) values (6403, 9406);
insert into country_nationality_join (country_id, nationality_id) values (6405, 9407);
insert into country_nationality_join (country_id, nationality_id) values (6406, 9408);
insert into country_nationality_join (country_id, nationality_id) values (6407, 9288);
insert into country_nationality_join (country_id, nationality_id) values (6410, 9409);
insert into country_nationality_join (country_id, nationality_id) values (6411, 9410);
insert into country_nationality_join (country_id, nationality_id) values (6412, 9411);
insert into country_nationality_join (country_id, nationality_id) values (6414, 9412);
insert into country_nationality_join (country_id, nationality_id) values (6415, 9413);
insert into country_nationality_join (country_id, nationality_id) values (6418, 9415);
insert into country_nationality_join (country_id, nationality_id) values (6419, 9416);
insert into country_nationality_join (country_id, nationality_id) values (6420, 9417);
insert into country_nationality_join (country_id, nationality_id) values (9565, 9566);
insert into country_nationality_join (country_id, nationality_id) values (1000, 9564);
insert into country_nationality_join (country_id, nationality_id) values (10000, 9321);
insert into country_nationality_join (country_id, nationality_id) values (10001, 9277);
