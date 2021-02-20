DROP TABLE IF EXISTS full_instrument_description;
CREATE TABLE full_instrument_description(figi VARCHAR(255) PRIMARY KEY,
                                         security_type VARCHAR(255),
                                         market_sector VARCHAR(255),
                                         ticker VARCHAR(255),
                                         name VARCHAR(255),
                                         uniqueid VARCHAR(255),
                                         exch_code VARCHAR(255),
                                         share_classfigi VARCHAR(255),
                                         compositefigi VARCHAR(255),
                                         security_type2 VARCHAR(255),
                                         security_description VARCHAR(255),
                                         uniqueidfut_opt VARCHAR(255));

