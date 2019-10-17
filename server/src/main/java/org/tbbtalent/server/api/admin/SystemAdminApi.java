package org.tbbtalent.server.api.admin;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.Gender;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.security.UserContext;

@RestController
@RequestMapping("/api/admin/system")
public class SystemAdminApi {

    private static final Logger log = LoggerFactory.getLogger(SystemAdminApi.class);

    private final UserContext userContext;

    private Timestamp now = Timestamp.valueOf(LocalDateTime.now());
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Map<Integer, Integer> countryForGeneralCountry;
    
    @Autowired
    public SystemAdminApi(UserContext userContext) {
        this.userContext = userContext;
        countryForGeneralCountry = getExtraCountryMappings();
    }

    public static void main(String[] args) {
        SystemAdminApi api = new SystemAdminApi(null);
        api.migrate();
    }
    
    @GetMapping("migrate")
    public String migrate() {
        try {
            // TODO: get value from logged in user when calling through api
//            User loggedInUser = userContext.getLoggedInUser();
            Long userId = 1L; // loggedInUser.getId();

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql://tbbtalent.org/yiitbb?useUnicode=yes&characterEncoding=UTF-8", "sayre", "MoroccoBound");
            Statement sourceStmt = sourceConn.createStatement();

            // TODO: get values from config file
            Connection targetConn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/tbbtalent", "tbbtalent", "tbbtalent");
            
            log.info("Preparing translations insert");
            PreparedStatement translationInsert = targetConn.prepareStatement("insert into translation (object_id, object_type, language, value, created_by, created_date) values (?, ?, ?, ?, ?, ?)");
            
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "country", "country", false);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "education_level", "education_level", true);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "education_major", "major", false);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "industry", "industry", false);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "language", "languages", false);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "language_level", "language_level", true);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "nationality", "nationality", false);
//            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "occupation", "job_ocupation", false);
            
//            migrateUsers(targetConn, sourceStmt);
//            migrateAdmins(targetConn, sourceStmt);
               
//            migrateCandidates(targetConn, sourceStmt);
            
            log.info("loading candidate ids");
            Map<Long, Long> candidateIdsByUserId = loadCandidateIds(targetConn);

//            migrateCandidateCertifications(targetConn, sourceStmt, candidateIdsByUserId);
//            migrateCandidateLanguages(targetConn, sourceStmt, candidateIdsByUserId);
//            migrateCandidateEducations(targetConn, sourceStmt, candidateIdsByUserId);
            
        } catch (Exception e){
            log.error("unable to migrate data", e);
        }

        return "done";
    }
    
    private void migrateFormOption(Connection targetConn,
                                   Statement sourceStmt,
                                   PreparedStatement translationInsert,
                                   Long userId,
                                   String tableName,
                                   String optionType,
                                   boolean hasLevel) throws SQLException {
        log.info("Migration data for " + tableName);
        String insertSql = null;
        String selectSql = null;
        if (hasLevel) {
            insertSql = "insert into " + tableName + " (id, name, level, status) values (?, ?, ?, ?) on conflict (id) do nothing";
            selectSql = "select id, name, name_ar, `order` from frm_options where type = '" + optionType + "'";
        } else {
            insertSql = "insert into " + tableName + " (id, name, status) values (?, ?, ?) on conflict (id) do nothing"; 
            selectSql = "select id, name, name_ar from frm_options where type = '" + optionType + "'";
        }
        
        PreparedStatement optionInsert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            Long id = result.getLong("id");
            optionInsert.setLong(i++, id);
            optionInsert.setString(i++, result.getString("name"));
            if (hasLevel) {
                optionInsert.setInt(i++, result.getInt("order"));
            }
            optionInsert.setString(i++, "active");
            optionInsert.addBatch();
            
            addTranslation(translationInsert, id, tableName, "ar", result.getString("name_ar"), userId);
            
            if (count%100 == 0) {
                optionInsert.executeBatch();
                translationInsert.executeBatch();
                log.info("saving batch " + count);
            }
            
            count++;
        }
        optionInsert.executeBatch();
        translationInsert.executeBatch();
        log.info("saving batch " + count);
    }
    
    private void addTranslation(PreparedStatement translationInsert,
                                Long objectId,
                                String objectType,
                                String language,
                                String value,
                                Long userId) throws SQLException {
        // object_id, object_type, language, value, created_by, created_date
        translationInsert.setLong(1, objectId);
        translationInsert.setString(2, objectType);
        translationInsert.setString(3, language);
        translationInsert.setString(4, value);
        translationInsert.setLong(5, 1L);
        translationInsert.setTimestamp(6, now);
        translationInsert.addBatch();
    }
    
    private void migrateUsers(Connection targetConn,
                              Statement sourceStmt) throws SQLException {
        log.info("Migration data for users from user");

        String insertSql = "insert into users (id, username, first_name, last_name, email, role, status, password_enc, created_by, created_date, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select u.id, username, j.first_name, j.last_name, email, status, password_hash, created_at, updated_at from user u join user_jobseeker j on j.user_id = u.id";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            insert.setLong(i++, result.getLong("id"));
            insert.setString(i++, result.getString("username"));
            insert.setString(i++, result.getString("first_name"));
            insert.setString(i++, result.getString("last_name"));
            insert.setString(i++, result.getString("email"));
            insert.setString(i++, "user");
            insert.setString(i++, getUserStatus(result.getInt("status")));
            insert.setString(i++, result.getString("password_hash"));
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.addBatch();
            
            if (count%100 == 0) {
                insert.executeBatch();
                log.info("saving batch " + count);
            }
            
            count++;
        }
        insert.executeBatch();
        log.info("saving batch " + count);
    }
    
    private void migrateAdmins(Connection targetConn,
                              Statement sourceStmt) throws SQLException {
        log.info("Migration data for users from admin");
        
        String insertSql = "insert into users (id, username, first_name, last_name, email, role, status, password_enc, created_by, created_date, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, username, email, status, password_hash, created_at, updated_at from admin;";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            insert.setLong(i++, result.getLong("id"));
            insert.setString(i++, result.getString("username"));
            insert.setNull(i++, Types.VARCHAR);
            insert.setNull(i++, Types.VARCHAR);
            insert.setString(i++, result.getString("email"));
            insert.setString(i++, "admin");
            insert.setString(i++, getUserStatus(result.getInt("status")));
            insert.setString(i++, result.getString("password_hash"));
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.addBatch();
            
            if (count%100 == 0) {
                insert.executeBatch();
                log.info("saving batch " + count);
            }
            
            count++;
        }
        insert.executeBatch();
        log.info("saving batch " + count);
    }
    
    private void migrateCandidates(Connection targetConn,
                                   Statement sourceStmt) throws SQLException {
        log.info("Migration data for candidates");
        
        log.info("loading reference data");
        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        Set<Long> nationalityIds = loadReferenceIds(targetConn, "nationality");
        Set<Long> eduLevelIds = loadReferenceIds(targetConn, "education_level");
        
        String insertSql = "insert into candidate (user_id, candidate_number, gender, dob, phone, whatsapp, status, country_id, "
                + " city, nationality_id, additional_info, max_education_level_id, created_by, created_date, updated_date) "
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) on conflict (user_id) do nothing";
        String selectSql = "select user_id, gender, concat(birth_year, '-', lpad(birth_month, 2, '0'), '-', lpad(birth_day, 2, '0')) as dob, "
                + " phone_number, phone_wapp, u.status, country, f.name as province, nationality, additional_information_summary, "
                + " current_education_level, u.created_at, u.updated_at "
                + " from user_jobseeker j join user u on u.id = j.user_id "
                + " left join frm_options f on f.id = j.province limit 10";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            int i = 1;
            insert.setLong(i++, result.getLong("user_id"));
            insert.setString(i++, result.getString("user_id"));
            insert.setString(i++, getGender(result.getString("gender")));
            insert.setDate(i++, convertToDate(result.getString("dob")));
            insert.setString(i++, result.getString("phone_number"));
            insert.setString(i++, result.getString("phone_wapp"));
            insert.setString(i++, getCandidateStatus(result.getInt("status")));
            i = setRefIdOrNull(result, insert, "country", countryIds, i);
            insert.setString(i++, result.getString("province"));
            i = setRefIdOrNull(result, insert, "nationality", nationalityIds, i);
            insert.setString(i++, result.getString("additional_information_summary"));
            i = setRefIdOrNull(result, insert, "current_education_level", eduLevelIds, i);
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.addBatch();
            
            if (count%100 == 0) {
                insert.executeBatch();
                log.info("saving batch " + count);
            }
            
            count++;
        }
        insert.executeBatch();
        log.info("saving batch " + count);
    }
    
    private void migrateCandidateCertifications(Connection targetConn,
                                                Statement sourceStmt,
                                                Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate certifications");
        String insertSql = "insert into candidate_certification (id, candidate_id, name, institution, date_completed) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, certification_name, institution_name, date_of_receipt from user_jobseeker_certification order by user_id limit 100";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setString(i++, result.getString("certification_name"));
                insert.setString(i++, result.getString("institution_name"));
                insert.setDate(i++, result.getDate("date_of_receipt"));
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("saving batch " + count);
                }
                
                count++;
            } else {
                log.warn("skipping record - certifications: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();
        log.info("saving batch " + count);
    }
    
    private void migrateCandidateLanguages(Connection targetConn,
                                           Statement sourceStmt,
                                           Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate languages");
        
        Set<Long> languageIds = loadReferenceIds(targetConn, "language");
        Set<Long> languageLevelIds = loadReferenceIds(targetConn, "language_level");
        
        String insertSql = "insert into candidate_language (id, candidate_id, language_id, written_level_id, spoken_level_id) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, language, level, level_reading, if_other from user_jobseeker_languages where user_id in (141, 169) order by user_id  limit 200";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            Long langId = checkReference(result.getInt("language"), languageIds);
            if (candidateId != null && langId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                i = setRefIdOrNull(result, insert, "language", languageIds, i);
                i = setRefIdOrNull(result, insert, "level", languageLevelIds, i);
                i = setRefIdOrNull(result, insert, "level_reading", languageLevelIds, i);
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("saving batch " + count);
                }
                
                count++;
            } else if (candidateId == null) {
                log.warn("skipping record - languages: no candidate found for userId " + userId );
            } else {
                log.warn("skipping record - languages: no language found for languageId " + langId );
            }
        }
        insert.executeBatch();
        log.info("saving batch " + count);
    }
    
    private void migrateCandidateEducations(Connection targetConn,
                                            Statement sourceStmt,
                                            Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate educations");
        
        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        
        String insertSql = "insert into candidate_education (id, candidate_id, country_id, institution, year_completed) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select j.id, user_id, country, f.name as university_school, graduation_year from user_jobseeker_education  j "
                + " left join frm_options f on f.id = j.university_school order by user_id limit 500";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            int origCountryId = result.getInt("country");
            Long countryId = checkReference(origCountryId, countryIds);
            if (countryId == null) {
                countryId = whackyExtraCountryLookup(origCountryId);
            }
            if (candidateId != null && countryId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setLong(i++, countryId);
                insert.setString(i++,  result.getString("university_school"));
                insert.setInt(i++, result.getInt("graduation_year"));
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("saving batch " + count);
                }
                
                count++;
            } else if (candidateId == null) {
                log.warn("skipping record - educations: no candidate found for userId " + userId );
            } else {
                log.warn("skipping record - educations: no country found for countryId " + countryId );
            }
        }
        insert.executeBatch();
        log.info("saving batch " + count);
    }

    private int setRefIdOrNull(ResultSet result,
                               PreparedStatement insert,
                               String columnName,
                               Set<Long> referenceIds,
                               int colIndex) throws SQLException {
        Long refId = checkReference(result.getInt(columnName), referenceIds);
        if (refId != null) {
            insert.setLong(colIndex, refId);
        } else {
            insert.setNull(colIndex,  Types.BIGINT);
        }
        return colIndex + 1;
    }
    
    private Long getCandidateId(Long userId,
                                  Map<Long, Long> candidateIdsByUserId) {
        return candidateIdsByUserId.get(userId);
    }

    private Set<Long> loadReferenceIds(Connection targetConn,
                                       String tableName) throws SQLException {
        Set<Long> referenceIds = new HashSet<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id from " + tableName);
        while (result.next()) {
            referenceIds.add(result.getLong(1));
        }
        log.info("loaded " + referenceIds.size() + " reference ids for " + tableName);
        return referenceIds;
    }
    
    private Map<Long, Long> loadCandidateIds(Connection targetConn) throws SQLException {
        Map<Long, Long> referenceMap = new HashMap<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id, user_id from candidate");
        while (result.next()) {
            referenceMap.put(result.getLong(2), result.getLong(1));
        }
        log.info("loaded " + referenceMap.size() + " candidate ids");
        return referenceMap;
    }
    
    private Long checkReference(int value,
                                Set<Long> referenceIds) {
        if (referenceIds.contains((long)value)) {
            return (long)value;
        }
        return null;
    }

    private String getUserStatus(Integer status) {
        /*
        0=Deleted
        1=Incomplete profile
        2=Awaiting permission
        3=Unable to contact
        4=Not in the region where TBB currently operates
        5=Not currently interested in international employment
        6=Profile recorded in 2 languages
        7=Misplaced or incorrect data
        8=Used for testing
        9=Pending
        10=Active
        11=Active but action needed to improve profile
        */
        switch (status) {
            case 0:
                return Status.deleted.name();
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return Status.inactive.name();
            case 10:
            case 11:
                return Status.active.name();
        }
        return Status.deleted.name();
    }

    private String getCandidateStatus(Integer status) {
        switch (status) {
            case 0:
                return CandidateStatus.deleted.name();
            case 1:
                return CandidateStatus.incomplete.name();
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return CandidateStatus.inactive.name();
            case 9:
                return CandidateStatus.pending.name();
            case 10:
            case 11:
                return CandidateStatus.active.name();
        }
        return CandidateStatus.deleted.name();
    }
    
    private String getGender(String gender) {
        if (StringUtils.isNotEmpty(gender)) {
            switch (gender) {
                case "M": return Gender.male.name();
                case "F": return Gender.female.name();
            }
        }
        return null;
    }
    
    private Long whackyExtraCountryLookup(Integer origCountryId) {
        Integer val = countryForGeneralCountry.get(origCountryId);
        if (val != null) return new Long(val); 
        return null;
    }

    private Timestamp convertToTimestamp(Long epoch) {
        return Timestamp.from(Instant.ofEpochSecond(epoch));
    }

    private Date convertToDate(String isoDateStr) {
        if (StringUtils.isNotEmpty(isoDateStr)) {
            try {
                java.util.Date date = sdf.parse(isoDateStr);
                return new Date(date.getTime());
            } catch (Exception e) {
                log.warn("invalid DOB " + isoDateStr); 
            }
        }
        return null;
    }

    private Map<Integer, Integer> getExtraCountryMappings() {
        
        /*
        +------+------+---------+---------+
        | id   | id   | name    | name    |
        +------+------+---------+---------+
        | 6288 |  358 | Jordan  | Jordan  |
        | 6296 |  359 | Lebanon | Lebanon |
        | 6400 |  360 | Turkey  | Turkey  |
        | 6389 |  361 | Syria   | Syria   |
        | 6243 |  363 | Egypt   | Egypt   |
        | 6262 | 6862 | Greece  | Greece  |
        | 6280 | 7344 | Iraq    | Iraq    |
        | 6327 | 9444 | Nauru   | Nauru   |
    */
        Map<Integer, Integer> countryForGeneralCountry = new HashMap<>();
        countryForGeneralCountry.put(358, 6288);
        countryForGeneralCountry.put(359, 6296);
        countryForGeneralCountry.put(360, 6400);
        countryForGeneralCountry.put(361, 6389);
        countryForGeneralCountry.put(363, 6243);
        countryForGeneralCountry.put(6862, 6262);
        countryForGeneralCountry.put(7344, 6280);
        countryForGeneralCountry.put(9444, 6327);
        return countryForGeneralCountry;
    }
}
