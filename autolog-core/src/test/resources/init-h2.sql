---
-- #%L
-- Autolog core module
-- %%
-- Copyright (C) 2019 - 2020 Maxime WIEWIORA
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---

---
-- Create database structure for testing of JdbcAdapter class.
---

-- Default table LOG_EVENTS.
DROP TABLE LOG_EVENTS IF EXISTS;
CREATE TABLE IF NOT EXISTS LOG_EVENTS (
    EVENT_TIMESTAMP TIMESTAMP NOT NULL,
    TOPIC VARCHAR(100) NOT NULL,
    LOG_LEVEL VARCHAR(5) NOT NULL,
    MESSAGE VARCHAR(1000)
);

-- Table LOG_EVENTS with prefix 'TEST'.
DROP TABLE TEST_LOG_EVENTS IF EXISTS;
CREATE TABLE IF NOT EXISTS TEST_LOG_EVENTS (
    EVENT_TIMESTAMP TIMESTAMP NOT NULL,
    TOPIC VARCHAR(100) NOT NULL,
    LOG_LEVEL VARCHAR(5) NOT NULL,
    MESSAGE VARCHAR(1000)
);