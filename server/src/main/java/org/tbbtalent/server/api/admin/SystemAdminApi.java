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
import java.text.DateFormat;
import java.text.ParseException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.AttachmentType;
import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.EducationType;
import org.tbbtalent.server.model.Gender;
import org.tbbtalent.server.model.NoteType;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.security.UserContext;

@RestController
@RequestMapping("/api/admin/system")
public class SystemAdminApi {

    private static final Logger log = LoggerFactory.getLogger(SystemAdminApi.class);

    private final UserContext userContext;
    final static String DATE_FORMAT = "dd-MM-yyyy";


    private Timestamp now = Timestamp.valueOf(LocalDateTime.now());
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Map<Integer, Integer> countryForGeneralCountry;
    
    @Value("${spring.datasource.url}")
    private String targetJdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String targetUser;
    
    @Value("${spring.datasource.password}")
    private String targetPwd;
    
    @Autowired
    public SystemAdminApi(UserContext userContext) {
        this.userContext = userContext;
        countryForGeneralCountry = getExtraCountryMappings();
    }

    public static void main(String[] args) {
        SystemAdminApi api = new SystemAdminApi(null);
        api.setTargetJdbcUrl("jdbc:postgresql://localhost:5432/tbbtalent");
        api.setTargetUser("tbbtalent");
        api.setTargetPwd("tbbtalent");
        api.migrate();
    }

    @GetMapping("migrate/status")
    public String migrateStatus() {
        try {
            Long userId = 1L;
            if (userContext != null) {
                User loggedInUser = userContext.getLoggedInUser();
                if (loggedInUser != null){
                    userId = loggedInUser.getId();
                }
            }

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql://v1.tbbtalent.org/yiitbb?useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", "sayre", "MoroccoBound");
            Statement sourceStmt = sourceConn.createStatement();

            Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUser, targetPwd);

            log.info("Migration data for candidates");

            String updateSql = "update candidate set migration_status =  ? where candidate_number = ? ";
            String selectSql = "select user_id, u.status from  user_jobseeker j join user u on u.id = j.user_id";
            PreparedStatement update = targetConn.prepareStatement(updateSql);
            ResultSet result = sourceStmt.executeQuery(selectSql);
            int count = 0;
            while (result.next()) {
                int i = 1;
                update.setString(i++, result.getString("status"));
                update.setString(i++, result.getString("user_id"));
                update.addBatch();

                if (count%100 == 0) {
                    update.executeBatch();
                    log.info("candidates - saving batch  " + count);
                }

                count++;
            }
            update.executeBatch();
            log.info("candidates - saving batch " + count);
            log.info("Migration data for candidates");

            updateSql = "update candidate set status =  'deleted' where migration_status = '0' ";
            update = targetConn.prepareStatement(updateSql);
            update.execute();
            log.info("candidates - fix deleted " + count);

        } catch (Exception e){
            log.error("unable to migrate status data", e);
        }

        return "done";
    }

    @GetMapping("migrate/survey")
    public String migrateSurvey() {
        try {
            Long userId = 1L;
            if (userContext != null) {
                User loggedInUser = userContext.getLoggedInUser();
                if (loggedInUser != null){
                    userId = loggedInUser.getId();
                }
            }

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql://v1.tbbtalent.org/yiitbb?useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", "sayre", "MoroccoBound");
            Statement sourceStmt = sourceConn.createStatement();

            Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUser, targetPwd);

            log.info("Migration survey data for candidates");

            String updateSql = "update candidate set survey_type_id = (" +
                    "    select id" +
                    "    from survey_type where name = ?" +
                    "    ), survey_comment = ? where candidate_number = ?";
            
            String selectSql = "select u.id as userID, o.name as optionName, " +
                    "       option_information_session, option_community_center, " +
                    "       option_facebook_page, option_other, option_ngo, option_outreach " +
                    "from user u " +
                    "    left join user_jobseeker_survey s on s.user_id = u.id" +
                    "    left join frm_options o on `option` = o.id " +
                    "where `option` is not null ";
            PreparedStatement update = targetConn.prepareStatement(updateSql);
            ResultSet result = sourceStmt.executeQuery(selectSql);
            int count = 0;
            while (result.next()) {
                String candidateNumber = result.getString("userID");
                String optionName = result.getString("optionName");
                
                String text = "";
                
                String s;
                s = result.getString("option_information_session");
                text += s == null ? "" : s;
                s = result.getString("option_community_center");
                text += s == null ? "" : s;
                s = result.getString("option_facebook_page");
                text += s == null ? "" : s;
                s = result.getString("option_other");
                text += s == null ? "" : s;
                s = result.getString("option_ngo");
                text += s == null ? "" : s;
                s = result.getString("option_outreach");
                text += s == null ? "" : s;
                
                int i = 1;
                update.setString(i++, optionName);
                update.setString(i++, text);
                update.setString(i++, candidateNumber);
                
                log.info("Update candidate " + candidateNumber + " " + optionName + ": " + text);
                update.addBatch();

//                if (count%100 == 0) {
                    update.executeBatch();
                    log.info("candidates - saving batch  " + count);
//                }

                break;
//                count++;
            }
//            update.executeBatch();
            log.info("candidates - saving batch " + count);
            log.info("Migration survey data for candidates");

        } catch (Exception e){
            log.error("unable to migrate survey data", e);
        }

        return "done";
    }

    //Hidden for now as should no longer be required - can probably delete
//    @GetMapping("migrate")
    public String migrate() {
        try {
            Long userId = 1L; 
            if (userContext != null) {
                User loggedInUser = userContext.getLoggedInUser();
                if (loggedInUser != null){
                    userId = loggedInUser.getId();
                }
            }

            Connection sourceConn = DriverManager.getConnection("jdbc:mysql://v1.tbbtalent.org/yiitbb?useUnicode=yes&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull", "sayre", "MoroccoBound");
            Statement sourceStmt = sourceConn.createStatement();

            Connection targetConn = DriverManager.getConnection(targetJdbcUrl, targetUser, targetPwd);
            
            log.info("Preparing translations insert");
            PreparedStatement translationInsert = targetConn.prepareStatement("insert into translation (object_id, object_type, language, value, created_by, created_date) values (?, ?, ?, ?, ?, ?)");
            
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "country", "country", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "education_level", "education_level", true);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "education_major", "major", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "industry", "industry", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "language", "languages", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "language_level", "language_level", true);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "nationality", "nationality", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "nationality", "nationality_other", false);
            migrateFormOption(targetConn, sourceStmt, translationInsert, userId, "occupation", "job_ocupation", false);

            migrateUsers(targetConn, sourceStmt);
            migrateAdmins(targetConn, sourceStmt);
               
            migrateCandidates(targetConn, sourceStmt);
            
            log.info("loading candidate ids");
            Map<Long, Long> candidateIdsByUserId = loadCandidateIds(targetConn);

            migrateCandidateCertifications(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateLanguages(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateEducations(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateOccupations(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateExperiences(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateAdminNotes(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateLinks(targetConn, sourceStmt, candidateIdsByUserId);
            migrateCandidateSkills(targetConn, sourceStmt, candidateIdsByUserId);

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
        if (optionType == "education_level"){
            insertSql = "insert into " + tableName + " (id, name, level, status, education_type) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
            selectSql = "select id, name, name_ar, `order` from frm_options where type = '" + optionType + "'";
        } else if (hasLevel) {
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
            String name = result.getString("name");
            optionInsert.setString(i++, name);
            if (hasLevel) {
                optionInsert.setInt(i++, result.getInt("order"));
            }
            optionInsert.setString(i++, "active");
            if (optionType == "education_level"){
                String educationType = getEducationType(name);
                if (educationType != null) {
                    optionInsert.setString(i++, educationType);
                } else {
                    optionInsert.setNull(i++, Types.VARCHAR);
                }
            }
            optionInsert.addBatch();
            
            addTranslation(translationInsert, id, tableName, "ar", result.getString("name_ar"), userId);
            
            if (count%100 == 0) {
                optionInsert.executeBatch();
                translationInsert.executeBatch();
                log.info(tableName + " - saving batch " + count);
            }
            
            count++;
        }
        optionInsert.executeBatch();
        translationInsert.executeBatch();
        log.info(tableName + " - saving batch " + count);



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
                log.info("users - saving batch " + count);
            }
            
            count++;
        }
        insert.executeBatch();
        log.info("users - saving batch " + count);
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
                log.info("admins - saving batch " + count);
            }
            
            count++;
        }
        insert.executeBatch();
        log.info("admins - saving batch " + count);
    }
    
    private void migrateCandidates(Connection targetConn,
                                   Statement sourceStmt) throws SQLException {
        log.info("Migration data for candidates");
        
        log.info("loading reference data");
        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        Set<Long> nationalityIds = loadReferenceIds(targetConn, "nationality");
        Set<Long> eduLevelIds = loadReferenceIds(targetConn, "education_level");
        Set<Long> eduMajorIds = loadReferenceIds(targetConn, "education_major");


        // load other options
        Map<Long, String> otherNationalities = loadOtherReferenceIds(sourceStmt, "nationality");
        
        String insertSql = "insert into candidate (user_id, candidate_number, gender, dob, phone, whatsapp, status, country_id, "
                + " city, nationality_id, additional_info, max_education_level_id, created_by, created_date, updated_date, un_registered, "
                + " un_registration_number, migration_education_major_id) "
                + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (user_id) do nothing";
        String selectSql = "select user_id, gender, concat(birth_year, '-', lpad(birth_month, 2, '0'), '-', lpad(birth_day, 2, '0')) as dob, "
                + " phone_number, phone_wapp, u.status, country, f.name as province, nationality, additional_information_summary, "
                + " current_education_level, u.created_at, u.updated_at, un_registration_status, unhcr_number, major "
                + " from user_jobseeker j join user u on u.id = j.user_id "
                + " left join frm_options f on f.id = j.province";
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
            insert.setString(i++, getCandidateStatus(result.getInt("status"), result.getInt("nationality"), result.getInt("country")));
            int origCountryId = result.getInt("country");
            Long countryId = checkReference(origCountryId, countryIds);
            if (countryId == null) {
                countryId = whackyExtraCountryLookup(origCountryId);
            }
            if (countryId != null) {
                insert.setLong(i++, countryId);
            } else {
                insert.setNull(i++,  Types.BIGINT);
            }
            insert.setString(i++, result.getString("province"));
            i = setRefIdOrUnknown(result, insert, "nationality", nationalityIds, otherNationalities, i, 18);
            insert.setString(i++, result.getString("additional_information_summary"));
            i = setRefIdOrNull(result, insert, "current_education_level", eduLevelIds, i);
            insert.setLong(i++, 1);
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
            insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
            insert.setBoolean(i++, getUNStatus(result.getInt("un_registration_status")));
            insert.setString(i++, result.getString("unhcr_number"));
            setRefIdOrNull(result, insert, "major", eduMajorIds, i++);
            insert.addBatch();
            
            if (count%100 == 0) {
                insert.executeBatch();
                log.info("candidates - saving batch  " + count);
            }
            
            count++;
        }
        insert.executeBatch();
        log.info("candidates - saving batch " + count);
    }

    private void migrateCandidateCertifications(Connection targetConn,
                                                Statement sourceStmt,
                                                Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate certifications");
        String insertSql = "insert into candidate_certification (id, candidate_id, name, institution, date_completed) values (?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, certification_name, institution_name, date_of_receipt from user_jobseeker_certification order by user_id";
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
                String certName = result.getString("certification_name");
                certName = certName.replaceAll("\u0000", "");
                insert.setString(i++, certName);
                String institutionName = result.getString("institution_name");
                institutionName = institutionName.replaceAll("\u0000", "");
                insert.setString(i++, institutionName);
                Date date = getDate(result, "date_of_receipt", candidateId);
                if (date != null) {
                    insert.setDate(i++, date);
                } else {
                    insert.setNull(i++, Types.DATE);
                }
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("certificates - saving batch " + count);
                }
                
                count++;
            } else {
                log.warn("skipping record - certifications: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();
        log.info("certificates - saving batch " + count);
    }
    
    private void migrateCandidateLanguages(Connection targetConn,
                                           Statement sourceStmt,
                                           Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate languages");
        
        Set<Long> languageIds = loadReferenceIds(targetConn, "language");
        Set<Long> languageLevelIds = loadReferenceIds(targetConn, "language_level");
        Map<Long, String> otherLanguages = loadOtherReferenceIds(sourceStmt, "language_other");

        String insertSql = "insert into candidate_language (id, candidate_id, language_id, written_level_id, spoken_level_id, migration_language) values (?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, language, level, level_reading, if_other from user_jobseeker_languages order by user_id";
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
                i = setRefIdOrUnknown(result, insert, "language", languageIds, otherLanguages, i, 6);
                i = setRefIdOrNull(result, insert, "level", languageLevelIds, i);
                i = setRefIdOrNull(result, insert, "level_reading", languageLevelIds, i);
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("languages - saving batch " + count);
                }
                
                count++;
            } else {
                log.warn("skipping record - languages: no candidate found for userId " + userId );
            }
        }
        insert.executeBatch();
        log.info("languages - saving batch " + count);
    }
    
    private void migrateCandidateEducations(Connection targetConn,
                                            Statement sourceStmt,
                                            Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate educations");
        
        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        
        String insertSql = "insert into candidate_education (id, candidate_id, country_id, institution, year_completed, education_type, course_name) values (?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select j.id, user_id, country, f.name as university_school, graduation_year, degree, specification_emphasis from user_jobseeker_education  j "
                + " left join frm_options f on f.id = j.university_school order by user_id";
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
                insert.setString(i++, getEducationLevel(result.getInt("degree")));
                insert.setString(i++,  result.getString("specification_emphasis"));
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("educations - saving batch " + count);
                }
                
                count++;
            } else if (candidateId == null) {
                log.warn("skipping record - educations: no candidate found for userId " + userId );
            } else {
                log.warn("skipping record - educations: no country found for countryId " + countryId );
            }
        }
        insert.executeBatch();
        log.info("educations - saving batch " + count);
    }

    private void migrateCandidateOccupations(Connection targetConn,
                                             Statement sourceStmt,
                                             Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate occupations");
        
        Set<Long> occupationIds = loadReferenceIds(targetConn, "occupation");
        Map<Long, String> otherOccupations = loadOtherReferenceIds(sourceStmt, "job_occupation");

        String insertSql = "insert into candidate_occupation (candidate_id, occupation_id, years_experience, verified, migration_occupation) values (?, ?, ?, ?, ?) on conflict (candidate_id, occupation_id) do nothing";  
        String selectSql = "select j.user_id, job_occupation, sum(timestampdiff(YEAR, ifnull(start_date, sysdate()), ifnull(end_date, sysdate()))) as years, case when u.status = 10 or u.status = 11 then 1 else 0 end as verified "
                        + " from user_jobseeker_experience j join user u on u.id = j.user_id group by j.user_id, job_occupation";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, candidateId);
                i = setRefIdOrUnknown(result, insert, "job_occupation", occupationIds, otherOccupations, i, 5);
                int years = result.getInt("years");
                if (years < 0) {
                    years = 0;
                }
                insert.setInt(i++, years);
                insert.setBoolean(i++, result.getBoolean("verified"));
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("occupations - saving batch " + count);
                }
                
                count++;
            } else {
                log.warn("skipping record - occupations: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();
        log.info("occupations - saving batch " + count);
    }
    
    private void migrateCandidateExperiences(Connection targetConn,
                                             Statement sourceStmt,
                                             Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate experiences");
        
        Set<Long> countryIds = loadReferenceIds(targetConn, "country");
        Set<Long> occupationIds = loadReferenceIds(targetConn, "occupation");
        // candidateId~occupationId -> candidateOccupationId
        Map<String, Long> candidateOccupations = loadCandidateOccupations(targetConn);
        
        String insertSql = "insert into candidate_job_experience (id, candidate_id, candidate_occupation_id, company_name, country_id, role, start_date, end_date, full_time, paid, description) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing"; 
        String selectSql = "select id, user_id, job_occupation, company_name, location, position_title, start_date, end_date, fulltime, paid, description from user_jobseeker_experience";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            int origOccupationId = result.getInt("job_occupation");
            Long occupationId = checkReference(origOccupationId, occupationIds);
            Long candidateOccupationId = candidateOccupations.get(candidateId + "~" + occupationId);
            if (candidateOccupationId == null) {
                // try with unknown
                candidateOccupationId = candidateOccupations.get(candidateId + "~0");
            }
            if (candidateId != null && candidateOccupationId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setLong(i++, candidateOccupationId);
                String companyName = result.getString("company_name");
                companyName = companyName.replaceAll("\u0000", "");
                insert.setString(i++,  companyName);
                i = setRefIdOrNull(result, insert, "location", countryIds, i);
                String position = result.getString("position_title");
                position = position.replaceAll("\u0000", "");
                insert.setString(i++,  position);
                Date date = getDate(result, "start_date", candidateId);
                if (date != null) {
                    insert.setDate(i++, date);
                } else {
                    insert.setNull(i++, Types.DATE);
                }
                date = getDate(result, "end_date", candidateId);
                if (date != null) {
                    insert.setDate(i++, date);
                } else {
                    insert.setNull(i++, Types.DATE);
                }
                insert.setBoolean(i++,  getFullTime(result.getInt("fulltime")));
                insert.setBoolean(i++,  getPaid(result.getInt("paid")));
                String description = result.getString("description");
                description = description.replaceAll("\u0000", "");
                insert.setString(i++,  description);

                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("experiences - saving batch " + count);
                }
                
                count++;
            } else if (candidateId == null) {
                log.warn("skipping record - experiences: no candidate found for userId " + userId);
            } else {
                log.warn("skipping record - experiences: no candidateOccupation found for candidateId " + candidateId + " and occupationId " + occupationId);
            }
        }
        insert.executeBatch();
        log.info("experiences - saving batch " + count);
    }

    private void migrateCandidateAdminNotes(Connection targetConn,
                                            Statement sourceStmt,
                                            Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate admin notes");
        
        Set<Long> adminIds = loadAdminIds(targetConn);
        
        String insertSql = "insert into candidate_note (id, candidate_id, note_type, title, comment, created_date, created_by, updated_date) values (?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, profile_id, subject, comments, created_at, updated_at from admin_user_notes";
        PreparedStatement insert = targetConn.prepareStatement(insertSql);
        ResultSet result = sourceStmt.executeQuery(selectSql);
        int count = 0;
        while (result.next()) {
            long userId = result.getLong("profile_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, result.getLong("id"));
                insert.setLong(i++, candidateId);
                insert.setString(i++, NoteType.admin.name());
                insert.setString(i++, result.getString("subject"));
                insert.setString(i++, result.getString("comments"));
                insert.setTimestamp(i++, convertToTimestamp(result.getLong("created_at")));
                long adminId = result.getLong("user_id");
                if (adminIds.contains(adminId)) {
                    insert.setLong(i++, adminId);
                } else {
                    insert.setNull(i++,  Types.BIGINT);
                }
                insert.setTimestamp(i++, convertToTimestamp(result.getLong("updated_at")));
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("admin notes - saving batch " + count);
                }
                
                count++;
            } else {
                log.warn("skipping record - admin notes: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();
        log.info("admin notes - saving batch " + count);
    }

    private void migrateCandidateLinks(Connection targetConn,
                                       Statement sourceStmt,
                                       Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate attachment links");

        String insertSql = "insert into candidate_attachment (id, candidate_id, type, name, location, migrated, admin_only, created_date, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, name, link from user_jobseeker_link";
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
                insert.setString(i++, AttachmentType.link.name());
                insert.setString(i++, result.getString("name"));
                insert.setString(i++, result.getString("link"));
                insert.setBoolean(i++, true);
                insert.setBoolean(i++, true);
                insert.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
                insert.setLong(i++, userId);
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("links - saving batch " + count);
                }

                count++;
            } else {
                log.warn("skipping record - attachment links: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();

        log.info("Migration data for candidate attachments");

        Set<Long> adminIds = loadAdminIds(targetConn);

        insertSql = "insert into candidate_attachment (candidate_id, type, name, location, file_type, migrated, admin_only, created_date, created_by) values (?, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (id) do nothing";
        selectSql = "select id, user_id, admin_id, filename, extension, upload_date from user_jobseeker_attachments";
        insert = targetConn.prepareStatement(insertSql);
        result = sourceStmt.executeQuery(selectSql);
        count = 0;
        while (result.next()) {
            long userId = result.getLong("user_id");
            Long candidateId = getCandidateId(userId, candidateIdsByUserId);
            if (candidateId != null) {
                int i = 1;
                insert.setLong(i++, candidateId);
                insert.setString(i++, AttachmentType.file.name());
                insert.setString(i++, result.getString("filename"));
                insert.setString(i++, result.getString("filename"));
                insert.setString(i++, result.getString("extension"));
                insert.setBoolean(i++, true);
                insert.setBoolean(i++, false);
                insert.setTimestamp(i++, convertToTimestamp(result.getLong("upload_date")));
                long adminId = result.getLong("user_id");
                if (adminIds.contains(adminId)) {
                    insert.setLong(i++, adminId);
                } else {
                    insert.setLong(i++, userId);
                }
                insert.addBatch();

                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("links - saving batch " + count);
                }

                count++;
            } else {
                log.warn("skipping record - attachment links: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();
        log.info("attachment links - saving batch " + count);
    }

    private void migrateCandidateSkills(Connection targetConn,
                                        Statement sourceStmt,
                                        Map<Long, Long> candidateIdsByUserId) throws SQLException {
        log.info("Migration data for candidate skills");
        
        String insertSql = "insert into candidate_skill (id, candidate_id, skill, time_period) values (?, ?, ?, ?) on conflict (id) do nothing";
        String selectSql = "select id, user_id, skill, time_period from user_jobseeker_skills";
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
                insert.setString(i++, result.getString("skill"));
                insert.setString(i++, getSkillTimePeriod(result.getInt("time_period")));
                insert.addBatch();
                
                if (count%100 == 0) {
                    insert.executeBatch();
                    log.info("skills - saving batch " + count);
                }
                
                count++;
            } else {
                log.warn("skipping record - skills: no candidate found for userId " + userId);
            }
        }
        insert.executeBatch();
        log.info("skills - saving batch " + count);
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

    private int setRefIdOrUnknown(ResultSet result,
                                  PreparedStatement insert,
                                  String columnName,
                                  Set<Long> referenceIds,
                                  Map<Long, String> otherValues,
                                  int colIndex,
                                  int otherColIndex) throws SQLException {
        int value = result.getInt(columnName);
        Long refId = checkReference(value, referenceIds);
        if (refId != null) {
            insert.setLong(colIndex, refId);
            insert.setNull(otherColIndex, Types.VARCHAR);
        } else if (otherValues.containsKey((long)value)) {
            insert.setLong(colIndex, 0);
            insert.setString(otherColIndex, otherValues.get((long)value));
        } else {
            insert.setNull(colIndex, Types.BIGINT);
            insert.setNull(otherColIndex, Types.VARCHAR);
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
    
    private Map<Long, String> loadOtherReferenceIds(Statement sourceStmt,
                                                    String type) throws SQLException {
        Map<Long, String> referenceIds = new HashMap<>();
        ResultSet result = sourceStmt.executeQuery("select id, name from frm_options_other where type = '" + type + "'");
        while (result.next()) {
            referenceIds.put(result.getLong(1), result.getString(2));
        }
        log.info("loaded " + referenceIds.size() + " other references for " + type);
        return referenceIds;
    }
    
    private Map<String, Long> loadCandidateOccupations(Connection targetConn) throws SQLException {
        Map<String, Long> candidateOccupations = new HashMap<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id, candidate_id, occupation_id from candidate_occupation");
        while (result.next()) {
            candidateOccupations.put(result.getLong(2) + "~" + result.getLong(3), result.getLong(1));
        }
        log.info("loaded " + candidateOccupations.size() + " candidateOccupationIds");
        return candidateOccupations;
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
    
    private Set<Long> loadAdminIds(Connection targetConn) throws SQLException {
        Set<Long> referenceMap = new HashSet<>();
        Statement stmt = targetConn.createStatement();
        ResultSet result = stmt.executeQuery("select id from users where role = 'admin'");
        while (result.next()) {
            referenceMap.add(result.getLong(1));
        }
        log.info("loaded " + referenceMap.size() + " admin ids");
        return referenceMap;
    }
    
    private Long checkReference(int value,
                                Set<Long> referenceIds) {
        // null values coming from source db are converted to integer 0 which would be incorrectly linked to "unknown", so treat as null
        Long lookupVal = value > 0 ? (long) value : null;
        if (referenceIds.contains(lookupVal)) {
            return lookupVal;
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
                return Status.inactive.name();
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return Status.active.name();
        }
        return Status.deleted.name();
    }

    private String getCandidateStatus(Integer status, Integer nationalityId, Integer countryId) {
        if (nationalityId == null || nationalityId == 0 || countryId == null || countryId == 0){
            return CandidateStatus.draft.name();
        }
        switch (status) {
            case 0:
                return CandidateStatus.deleted.name();
            case 1:
                return CandidateStatus.incomplete.name();
            case 2:
                return CandidateStatus.pending.name();
            case 3:
            case 4:
            case 5:
            case 6:
                return CandidateStatus.inactive.name();
            case 7:
                return CandidateStatus.incomplete.name();
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
    
    private boolean getUNStatus(int value) {
        // 1=UNHCR, 2=UNRWA, 3=not registered
        return (value == 1 ||  value == 2);
    }
    
    private String getEducationLevel(Integer value) {
        /*
        | 6864 | degree | Bachelor's Degree  |
        | 6865 | degree | Master's Degree    |
        | 6867 | degree | Doctoral Degree    |
        | 6868 | degree | Associate Degree   |
        | 9442 | degree | Vocational Degree  |
        |    0 | 

        */
        if (value > 0) {
            switch (value) {
                case 6864: return EducationType.Bachelor.name();
                case 6865: return EducationType.Masters.name();
                case 6867: return EducationType.Doctoral.name();
                case 6868: return EducationType.Associate.name();
                case 9442: return EducationType.Vocational.name();
            }
        }
        return null;
    }

    private String getEducationType(String name) {
        if (name != null) {
            switch (name) {
                case "Bachelor's Degree": return EducationType.Bachelor.name();
                case "Master's Degree": return EducationType.Masters.name();
                case "Doctoral Degree": return EducationType.Doctoral.name();
                case "Associate Degree": return EducationType.Associate.name();
                case "Vocational Degree": return EducationType.Vocational.name();
                case "Some University": return EducationType.Bachelor.name();
            }
        }
        return null;
    }

    private boolean getPaid(int paidValue) {
        /*
        | 9561 | fulltime | Full time |
        | 9562 | fulltime | Part time |
        */
        return (paidValue == 9557);
    }
    
    private boolean getFullTime(int fullTimeValue) {
        /*
        | 9561 | fulltime | Full time |
        | 9562 | fulltime | Part time |
        */
        return (fullTimeValue == 9561);
    }
    
    private String getSkillTimePeriod(int period) {
        /*
        | 336 | time_period | 1 year or less   |
        | 337 | time_period | 1-2 years        |
        | 338 | time_period | 3-5 years        |
        | 339 | time_period | 5-7 years        |
        | 340 | time_period | 7-9 years        |
        | 341 | time_period | 10 years or more |
         */
        if (period > 0) {
            switch (period) {
                case 336: return "1 year or less";
                case 337: return "1-2 years";
                case 338: return "3-5 years";
                case 339: return "5-7 years";
                case 340: return "7-9 years";
                case 341: return "10 years or more";
            }
        }
        return null;
    }
    
    private Long whackyExtraCountryLookup(Integer origCountryId) {
        Integer val = countryForGeneralCountry.get(origCountryId);
        if (val != null) return new Long(val); 
        return null;
    }
    
    private Date getDate(ResultSet result,
                         String columnName,
                         Long candidateId) {
        try {
            Date date = result.getDate(columnName);
            if (date != null && !"1970-01-01".equals(date.toString())) {
                return date;
            }
        } catch (Exception e) {
            try {
                log.error("candidateId " + candidateId + " - unparsable date for column: " + columnName + " - " + result.getString(columnName), e);
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Timestamp convertToTimestamp(Long epoch) {
        if (epoch != null && epoch > 0) {
            epoch = epoch*1000;
            java.util.Date date = new java.util.Date(epoch);
            String formattedDate = new SimpleDateFormat(DATE_FORMAT).format(date);
            if (isDateValid(formattedDate)){
                return Timestamp.from(Instant.ofEpochMilli(epoch));
            };
        }
        return null;
    }

    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
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

    public void setTargetJdbcUrl(String targetJdbcUrl) {
        this.targetJdbcUrl = targetJdbcUrl;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public void setTargetPwd(String targetPwd) {
        this.targetPwd = targetPwd;
    }


}
