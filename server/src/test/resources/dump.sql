--
-- PostgreSQL database dump
--

-- Dumped from database version 14.12 (Debian 14.12-1.pgdg120+1)
-- Dumped by pg_dump version 16.7

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: tctalent
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO tctalent;

--
-- Name: mergeoccupations(bigint, bigint); Type: PROCEDURE; Schema: public; Owner: tctalent
--

CREATE PROCEDURE public.mergeoccupations(IN target bigint, IN remove bigint)
    LANGUAGE sql
    AS $$
    
-- Make all job experiences point to the first occupation 
update candidate_job_experience set candidate_occupation_id =
                                        (select id from candidate_occupation
                                         where candidate_occupation.occupation_id = target
                                           and candidate_occupation.candidate_id = candidate_job_experience.candidate_id)
where candidate_occupation_id =
      (select id from candidate_occupation
       where candidate_occupation.occupation_id = remove
         and candidate_occupation.candidate_id = candidate_job_experience.candidate_id)
  and candidate_job_experience.candidate_id in
      (select A.candidate_id from
          (select * from candidate_occupation where occupation_id = target) as A
              join
          (select * from candidate_occupation where occupation_id = remove) as B
          on A.candidate_id = B.candidate_id);

-- Delete the now unused occupation - no job experiences
delete from candidate_occupation where occupation_id = remove and candidate_id in
                                                                (select A.candidate_id from
                                                                    (select * from candidate_occupation where occupation_id = target) as A
                                                                        join
                                                                    (select * from candidate_occupation where occupation_id = remove) as B
                                                                    on A.candidate_id = B.candidate_id);

-- Update all the other candidate_occupations that don't have both ids
update candidate_occupation set occupation_id = target where occupation_id = remove;
    
-- Delete the removed occupation
delete from occupation where id = remove;
    
$$;


ALTER PROCEDURE public.mergeoccupations(IN target bigint, IN remove bigint) OWNER TO tctalent;

--
-- Name: showdupcandidates(integer, integer); Type: PROCEDURE; Schema: public; Owner: tctalent
--

CREATE PROCEDURE public.showdupcandidates(IN target integer, IN remove integer)
    LANGUAGE sql
    AS $$
   select A.candidate_id from
    (select * from candidate_occupation where occupation_id = target) as A
        join
    (select * from candidate_occupation where occupation_id = remove) as B
    on A.candidate_id = B.candidate_id;
$$;


ALTER PROCEDURE public.showdupcandidates(IN target integer, IN remove integer) OWNER TO tctalent;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.audit_log (
    id bigint NOT NULL,
    event_date date NOT NULL,
    user_id bigint NOT NULL,
    type text NOT NULL,
    action text NOT NULL,
    object_ref text NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.audit_log OWNER TO tctalent;

--
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_log_id_seq OWNER TO tctalent;

--
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- Name: candidate; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate (
    id bigint NOT NULL,
    candidate_number text,
    user_id bigint,
    gender text,
    dob date,
    phone text,
    whatsapp text,
    status text NOT NULL,
    country_id bigint,
    city text,
    year_of_arrival integer,
    nationalityold_id bigint,
    unhcr_status text,
    unhcr_number text,
    additional_info text,
    max_education_level_id bigint,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    updated_by bigint,
    updated_date timestamp with time zone,
    address1 text,
    candidate_message text,
    preferred_language text,
    migration_status text,
    sflink text,
    folderlink text,
    videolink text,
    survey_type_id bigint,
    survey_comment text,
    text_search_id text,
    returned_home text,
    returned_home_reason text,
    visa_issues text,
    visa_issues_notes text,
    avail_immediate text,
    avail_immediate_reason text,
    avail_immediate_notes text,
    family_move text,
    family_move_notes text,
    int_recruit_reasons text,
    int_recruit_rural text,
    return_home_safe text,
    work_permit text,
    work_permit_desired text,
    work_desired text,
    asylum_year date,
    home_location text,
    unhcr_not_reg_status text,
    unhcr_file integer,
    unhcr_notes text,
    unhcr_permission text,
    unrwa_number text,
    unrwa_notes text,
    dest_limit text,
    dest_limit_notes text,
    crime_convict text,
    crime_convict_notes text,
    conflict text,
    conflict_notes text,
    residence_status text,
    work_abroad text,
    host_entry_legally text,
    left_home_notes text,
    return_home_future text,
    return_home_when text,
    resettle_third text,
    resettle_third_status text,
    host_challenges text,
    marital_status text,
    partner_registered text,
    partner_candidate_id bigint,
    partner_edu_level_id bigint,
    partner_occupation_id bigint,
    partner_english text,
    partner_english_level_id bigint,
    partner_ielts text,
    partner_ielts_score text,
    partner_citizenship text,
    military_service text,
    visa_reject text,
    can_drive text,
    driving_license text,
    driving_license_exp date,
    driving_license_country_id bigint,
    host_entry_year integer,
    english_assessment text,
    english_assessment_score_ielts text,
    int_recruit_rural_notes text,
    work_abroad_notes text,
    host_entry_legally_notes text,
    visa_reject_notes text,
    partner_ielts_yr integer,
    partner_edu_level_notes text,
    partner_occupation_notes text,
    returned_home_reason_no text,
    residence_status_notes text,
    work_desired_notes text,
    left_home_reasons text,
    military_wanted text,
    military_notes text,
    military_start date,
    military_end date,
    int_recruit_other text,
    avail_immediate_job_ops text,
    unhcr_registered text,
    unrwa_registered text,
    birth_country_id bigint,
    linked_in_link text,
    marital_status_notes text,
    work_permit_desired_notes text,
    host_entry_year_notes text,
    unrwa_file integer,
    unrwa_not_reg_status text,
    ielts_score numeric,
    nationality_id bigint,
    health_issues text,
    health_issues_notes text,
    unhcr_consent text,
    shareable_notes text,
    shareable_cv_attachment_id bigint,
    shareable_doc_attachment_id bigint,
    mini_intake date,
    full_intake date,
    state text,
    external_id text,
    external_id_source text,
    covid_vaccinated text,
    covid_vaccinated_status text,
    covid_vaccinated_date date,
    covid_vaccine_name text,
    covid_vaccine_notes text,
    folderlink_address text,
    folderlink_character text,
    folderlink_employer text,
    folderlink_identity text,
    folderlink_medical text,
    folderlink_qualification text,
    folderlink_registration text,
    media_willingness text,
    folderlink_engagement text,
    folderlink_experience text,
    folderlink_family text,
    folderlink_immigration text,
    folderlink_language text,
    partner_ref text,
    rego_ip text,
    rego_utm_campaign text,
    rego_utm_content text,
    rego_utm_medium text,
    rego_utm_source text,
    rego_utm_term text,
    rego_partner_param text,
    rego_referrer_param text,
    contact_consent_registration boolean DEFAULT false NOT NULL,
    contact_consent_partners boolean DEFAULT false NOT NULL,
    monitoring_evaluation_consent text,
    mini_intake_completed_by bigint,
    mini_intake_completed_date timestamp with time zone,
    full_intake_completed_by bigint,
    full_intake_completed_date timestamp with time zone,
    french_assessment text,
    french_assessment_score_nclc integer,
    arrest_imprison text,
    arrest_imprison_notes text,
    avail_date date,
    potential_duplicate boolean DEFAULT false NOT NULL,
    public_id character varying(22),
    relocated_address text,
    relocated_city text,
    relocated_state text,
    relocated_country_id bigint,
    english_assessment_score_det integer,
    muted boolean DEFAULT false NOT NULL,
    all_notifications boolean DEFAULT false NOT NULL,
    registered_by bigint,
    change_password boolean DEFAULT false
);


ALTER TABLE public.candidate OWNER TO tctalent;

--
-- Name: candidate_attachment; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_attachment (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    type text,
    name text,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone,
    location text NOT NULL,
    migrated boolean DEFAULT false NOT NULL,
    file_type text,
    text_extract text,
    cv boolean DEFAULT false NOT NULL,
    upload_type text
);


ALTER TABLE public.candidate_attachment OWNER TO tctalent;

--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_attachment_id_seq OWNER TO tctalent;

--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_attachment_id_seq OWNED BY public.candidate_attachment.id;


--
-- Name: candidate_certification; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_certification (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    name text,
    institution text,
    date_completed date
);


ALTER TABLE public.candidate_certification OWNER TO tctalent;

--
-- Name: candidate_certification_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_certification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_certification_id_seq OWNER TO tctalent;

--
-- Name: candidate_certification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_certification_id_seq OWNED BY public.candidate_certification.id;


--
-- Name: candidate_citizenship; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_citizenship (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    nationality_id bigint,
    has_passport text,
    notes text,
    passport_exp date
);


ALTER TABLE public.candidate_citizenship OWNER TO tctalent;

--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_citizenship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_citizenship_id_seq OWNER TO tctalent;

--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_citizenship_id_seq OWNED BY public.candidate_citizenship.id;


--
-- Name: candidate_coupon_code; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_coupon_code (
    id bigint NOT NULL,
    offer_to_assist_id bigint,
    candidate_id bigint NOT NULL,
    coupon_code text
);


ALTER TABLE public.candidate_coupon_code OWNER TO tctalent;

--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_coupon_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_coupon_code_id_seq OWNER TO tctalent;

--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_coupon_code_id_seq OWNED BY public.candidate_coupon_code.id;


--
-- Name: candidate_dependant; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_dependant (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    relation text,
    dob date,
    health_concern text,
    health_notes text,
    name text,
    registered text,
    relation_other text,
    registered_number text,
    registered_notes text,
    gender text
);


ALTER TABLE public.candidate_dependant OWNER TO tctalent;

--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_dependant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_dependant_id_seq OWNER TO tctalent;

--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_dependant_id_seq OWNED BY public.candidate_dependant.id;


--
-- Name: candidate_destination; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_destination (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    country_id bigint,
    interest text,
    family text,
    location text,
    notes text
);


ALTER TABLE public.candidate_destination OWNER TO tctalent;

--
-- Name: candidate_destination_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_destination_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_destination_id_seq OWNER TO tctalent;

--
-- Name: candidate_destination_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_destination_id_seq OWNED BY public.candidate_destination.id;


--
-- Name: candidate_education; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_education (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    education_type text,
    country_id bigint NOT NULL,
    length_of_course_years integer,
    institution text,
    course_name text,
    year_completed integer,
    major_id bigint,
    incomplete boolean
);


ALTER TABLE public.candidate_education OWNER TO tctalent;

--
-- Name: candidate_education_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_education_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_education_id_seq OWNER TO tctalent;

--
-- Name: candidate_education_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_education_id_seq OWNED BY public.candidate_education.id;


--
-- Name: candidate_exam; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_exam (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    exam text,
    other_exam text,
    score text,
    year integer,
    notes text
);


ALTER TABLE public.candidate_exam OWNER TO tctalent;

--
-- Name: candidate_exam_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_exam_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_exam_id_seq OWNER TO tctalent;

--
-- Name: candidate_exam_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_exam_id_seq OWNED BY public.candidate_exam.id;


--
-- Name: candidate_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_id_seq OWNER TO tctalent;

--
-- Name: candidate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_id_seq OWNED BY public.candidate.id;


--
-- Name: candidate_job_experience; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_job_experience (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    candidate_occupation_id bigint NOT NULL,
    company_name text,
    country_id bigint,
    role text,
    start_date date,
    end_date date,
    full_time boolean,
    paid boolean,
    description text
);


ALTER TABLE public.candidate_job_experience OWNER TO tctalent;

--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_job_experience_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_job_experience_id_seq OWNER TO tctalent;

--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_job_experience_id_seq OWNED BY public.candidate_job_experience.id;


--
-- Name: candidate_language; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_language (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    language_id bigint,
    written_level_id bigint,
    spoken_level_id bigint
);


ALTER TABLE public.candidate_language OWNER TO tctalent;

--
-- Name: candidate_language_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_language_id_seq OWNER TO tctalent;

--
-- Name: candidate_language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_language_id_seq OWNED BY public.candidate_language.id;


--
-- Name: candidate_note; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_note (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    note_type text NOT NULL,
    title text NOT NULL,
    comment text,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.candidate_note OWNER TO tctalent;

--
-- Name: candidate_note_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_note_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_note_id_seq OWNER TO tctalent;

--
-- Name: candidate_note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_note_id_seq OWNED BY public.candidate_note.id;


--
-- Name: candidate_occupation; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_occupation (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    occupation_id bigint,
    years_experience integer NOT NULL,
    top_candidate boolean,
    migration_occupation text,
    updated_by bigint,
    updated_date timestamp with time zone,
    created_by bigint,
    created_date timestamp with time zone
);


ALTER TABLE public.candidate_occupation OWNER TO tctalent;

--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_occupation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_occupation_id_seq OWNER TO tctalent;

--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_occupation_id_seq OWNED BY public.candidate_occupation.id;


--
-- Name: candidate_opportunity; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_opportunity (
    id bigint NOT NULL,
    candidate_id bigint,
    closing_comments_for_candidate text,
    employer_feedback text,
    job_opp_id bigint,
    stage text,
    closing_comments text,
    closed boolean DEFAULT false NOT NULL,
    name text,
    next_step text,
    next_step_due_date date,
    sf_id text,
    stage_order integer,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone,
    won boolean DEFAULT false NOT NULL,
    file_offer_link text,
    file_offer_name text,
    last_active_stage text DEFAULT 'prospect'::text,
    relocating_dependant_ids text
);


ALTER TABLE public.candidate_opportunity OWNER TO tctalent;

--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_opportunity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_opportunity_id_seq OWNER TO tctalent;

--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_opportunity_id_seq OWNED BY public.candidate_opportunity.id;


--
-- Name: candidate_property; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_property (
    candidate_id bigint NOT NULL,
    name text NOT NULL,
    value text,
    related_task_assignment_id bigint
);


ALTER TABLE public.candidate_property OWNER TO tctalent;

--
-- Name: candidate_review_item; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_review_item (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    saved_search_id bigint NOT NULL,
    review_status text NOT NULL,
    comment text,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.candidate_review_item OWNER TO tctalent;

--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_review_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_review_item_id_seq OWNER TO tctalent;

--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_review_item_id_seq OWNED BY public.candidate_review_item.id;


--
-- Name: candidate_saved_list; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_saved_list (
    candidate_id bigint NOT NULL,
    saved_list_id bigint NOT NULL,
    context_note text,
    shareable_cv_attachment_id bigint,
    shareable_doc_attachment_id bigint
);


ALTER TABLE public.candidate_saved_list OWNER TO tctalent;

--
-- Name: candidate_skill; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_skill (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    skill text,
    time_period character varying(100)
);


ALTER TABLE public.candidate_skill OWNER TO tctalent;

--
-- Name: candidate_skill_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_skill_id_seq OWNER TO tctalent;

--
-- Name: candidate_skill_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_skill_id_seq OWNED BY public.candidate_skill.id;


--
-- Name: candidate_visa_check; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_visa_check (
    id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    country_id bigint,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone,
    protection text,
    protection_grounds text,
    health_assessment text,
    health_assessment_notes text,
    character_assessment text,
    character_assessment_notes text,
    security_risk text,
    security_risk_notes text,
    valid_travel_docs text,
    valid_travel_docs_notes text,
    overall_risk text,
    overall_risk_notes text,
    english_threshold text,
    english_threshold_notes text,
    pathway_assessment text,
    pathway_assessment_notes text,
    destination_family text,
    destination_family_location text
);


ALTER TABLE public.candidate_visa_check OWNER TO tctalent;

--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_visa_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_visa_check_id_seq OWNER TO tctalent;

--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_visa_check_id_seq OWNED BY public.candidate_visa_check.id;


--
-- Name: candidate_visa_job_check; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.candidate_visa_job_check (
    id bigint NOT NULL,
    interest text,
    interest_notes text,
    occupation_id bigint,
    occupation_notes text,
    salary_tsmit text,
    regional text,
    eligible_494 text,
    eligible_494_notes text,
    eligible_186 text,
    eligible_186_notes text,
    eligible_other text,
    eligible_other_notes text,
    put_forward text,
    notes text,
    candidate_visa_check_id bigint,
    tbb_eligibility text,
    qualification text,
    qualification_notes text,
    job_opp_id bigint,
    relevant_work_exp text,
    age_requirement text,
    preferred_pathways text,
    ineligible_pathways text,
    eligible_pathways text,
    occupation_category text,
    occupation_sub_category text,
    english_threshold text,
    languages_threshold_notes text,
    languages_required text,
    languages_threshold_met text
);


ALTER TABLE public.candidate_visa_job_check OWNER TO tctalent;

--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.candidate_visa_job_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_visa_job_check_id_seq OWNER TO tctalent;

--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.candidate_visa_job_check_id_seq OWNED BY public.candidate_visa_job_check.id;


--
-- Name: chat_post; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.chat_post (
    id bigint NOT NULL,
    content text,
    job_chat_id bigint NOT NULL,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.chat_post OWNER TO tctalent;

--
-- Name: chat_post_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.chat_post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.chat_post_id_seq OWNER TO tctalent;

--
-- Name: chat_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.chat_post_id_seq OWNED BY public.chat_post.id;


--
-- Name: country; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.country (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL,
    iso_code text
);


ALTER TABLE public.country OWNER TO tctalent;

--
-- Name: country_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.country_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.country_id_seq OWNER TO tctalent;

--
-- Name: country_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.country_id_seq OWNED BY public.country.id;


--
-- Name: country_nationality_join; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.country_nationality_join (
    country_id bigint NOT NULL,
    nationality_id bigint NOT NULL
);


ALTER TABLE public.country_nationality_join OWNER TO tctalent;

--
-- Name: duolingo_coupon; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.duolingo_coupon (
    id bigint NOT NULL,
    coupon_code character varying(255) NOT NULL,
    candidate_id bigint,
    expiration_date timestamp with time zone NOT NULL,
    date_sent timestamp with time zone,
    coupon_status character varying(50) NOT NULL,
    test_type character varying(20)
);


ALTER TABLE public.duolingo_coupon OWNER TO tctalent;

--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.duolingo_coupon_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duolingo_coupon_id_seq OWNER TO tctalent;

--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.duolingo_coupon_id_seq OWNED BY public.duolingo_coupon.id;


--
-- Name: duolingo_extra_fields; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.duolingo_extra_fields (
    id bigint NOT NULL,
    certificate_url character varying(255),
    interview_url character varying(255),
    verification_date character varying(255),
    percent_score integer NOT NULL,
    scale integer NOT NULL,
    literacy_subscore integer NOT NULL,
    conversation_subscore integer NOT NULL,
    comprehension_subscore integer NOT NULL,
    production_subscore integer NOT NULL,
    candidate_exam_id bigint NOT NULL
);


ALTER TABLE public.duolingo_extra_fields OWNER TO tctalent;

--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.duolingo_extra_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duolingo_extra_fields_id_seq OWNER TO tctalent;

--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.duolingo_extra_fields_id_seq OWNED BY public.duolingo_extra_fields.id;


--
-- Name: education_level; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.education_level (
    id bigint NOT NULL,
    name text NOT NULL,
    level integer NOT NULL,
    status text DEFAULT 'active'::text NOT NULL,
    education_type text
);


ALTER TABLE public.education_level OWNER TO tctalent;

--
-- Name: education_level_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.education_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.education_level_id_seq OWNER TO tctalent;

--
-- Name: education_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.education_level_id_seq OWNED BY public.education_level.id;


--
-- Name: education_major; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.education_major (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL
);


ALTER TABLE public.education_major OWNER TO tctalent;

--
-- Name: education_major_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.education_major_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.education_major_id_seq OWNER TO tctalent;

--
-- Name: education_major_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.education_major_id_seq OWNED BY public.education_major.id;


--
-- Name: employer; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.employer (
    id bigint NOT NULL,
    country_id bigint,
    description text,
    has_hired_internationally boolean,
    name text,
    sf_id text,
    website text,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.employer OWNER TO tctalent;

--
-- Name: employer_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.employer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employer_id_seq OWNER TO tctalent;

--
-- Name: employer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.employer_id_seq OWNED BY public.employer.id;


--
-- Name: export_column; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.export_column (
    id bigint NOT NULL,
    saved_list_id bigint,
    saved_search_id bigint,
    index integer NOT NULL,
    key text,
    properties text
);


ALTER TABLE public.export_column OWNER TO tctalent;

--
-- Name: export_column_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.export_column_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.export_column_id_seq OWNER TO tctalent;

--
-- Name: export_column_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.export_column_id_seq OWNED BY public.export_column.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO tctalent;

--
-- Name: help_link; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.help_link (
    id bigint NOT NULL,
    country_id bigint,
    case_stage text,
    job_stage text,
    label text NOT NULL,
    link text NOT NULL,
    focus text,
    next_step_name text,
    next_step_text text,
    next_step_days integer,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.help_link OWNER TO tctalent;

--
-- Name: help_link_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.help_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.help_link_id_seq OWNER TO tctalent;

--
-- Name: help_link_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.help_link_id_seq OWNED BY public.help_link.id;


--
-- Name: industry; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.industry (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL
);


ALTER TABLE public.industry OWNER TO tctalent;

--
-- Name: industry_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.industry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.industry_id_seq OWNER TO tctalent;

--
-- Name: industry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.industry_id_seq OWNED BY public.industry.id;


--
-- Name: job_chat; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.job_chat (
    id bigint NOT NULL,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    job_id bigint,
    updated_by bigint,
    updated_date timestamp with time zone,
    type text,
    source_partner_id bigint,
    candidate_id bigint
);


ALTER TABLE public.job_chat OWNER TO tctalent;

--
-- Name: job_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.job_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.job_chat_id_seq OWNER TO tctalent;

--
-- Name: job_chat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.job_chat_id_seq OWNED BY public.job_chat.id;


--
-- Name: job_chat_user; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.job_chat_user (
    job_chat_id bigint NOT NULL,
    user_id bigint NOT NULL,
    last_read_post_id bigint
);


ALTER TABLE public.job_chat_user OWNER TO tctalent;

--
-- Name: job_opp_intake; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.job_opp_intake (
    id bigint NOT NULL,
    recruitment_process text,
    employer_cost_commitment text,
    location text,
    location_details text,
    salary_range text,
    benefits text,
    language_requirements text,
    employment_experience text,
    education_requirements text,
    skill_requirements text,
    occupation_code text,
    min_salary text,
    visa_pathways text
);


ALTER TABLE public.job_opp_intake OWNER TO tctalent;

--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.job_opp_intake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.job_opp_intake_id_seq OWNER TO tctalent;

--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.job_opp_intake_id_seq OWNED BY public.job_opp_intake.id;


--
-- Name: job_suggested_saved_search; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.job_suggested_saved_search (
    tc_job_id bigint NOT NULL,
    saved_search_id bigint NOT NULL
);


ALTER TABLE public.job_suggested_saved_search OWNER TO tctalent;

--
-- Name: language; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.language (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL,
    iso_code text
);


ALTER TABLE public.language OWNER TO tctalent;

--
-- Name: language_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.language_id_seq OWNER TO tctalent;

--
-- Name: language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.language_id_seq OWNED BY public.language.id;


--
-- Name: language_level; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.language_level (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL,
    level integer NOT NULL
);


ALTER TABLE public.language_level OWNER TO tctalent;

--
-- Name: language_level_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.language_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.language_level_id_seq OWNER TO tctalent;

--
-- Name: language_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.language_level_id_seq OWNED BY public.language_level.id;


--
-- Name: link_preview; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.link_preview (
    id bigint NOT NULL,
    chat_post_id bigint,
    url text,
    title text,
    description text,
    image_url text,
    domain text,
    favicon_url text
);


ALTER TABLE public.link_preview OWNER TO tctalent;

--
-- Name: link_preview_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.link_preview_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.link_preview_id_seq OWNER TO tctalent;

--
-- Name: link_preview_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.link_preview_id_seq OWNED BY public.link_preview.id;


--
-- Name: nationality; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.nationality (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL
);


ALTER TABLE public.nationality OWNER TO tctalent;

--
-- Name: nationality_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.nationality_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.nationality_id_seq OWNER TO tctalent;

--
-- Name: nationality_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.nationality_id_seq OWNED BY public.nationality.id;


--
-- Name: occupation; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.occupation (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL,
    isco08_code text
);


ALTER TABLE public.occupation OWNER TO tctalent;

--
-- Name: occupation_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.occupation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.occupation_id_seq OWNER TO tctalent;

--
-- Name: occupation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.occupation_id_seq OWNED BY public.occupation.id;


--
-- Name: offer_to_assist; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.offer_to_assist (
    id bigint NOT NULL,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    updated_by bigint,
    updated_date timestamp with time zone,
    additional_notes text,
    partner_id bigint NOT NULL,
    public_id character varying(22),
    reason text
);


ALTER TABLE public.offer_to_assist OWNER TO tctalent;

--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.offer_to_assist_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.offer_to_assist_id_seq OWNER TO tctalent;

--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.offer_to_assist_id_seq OWNED BY public.offer_to_assist.id;


--
-- Name: partner; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.partner (
    default_source_partner boolean DEFAULT false NOT NULL,
    id bigint NOT NULL,
    logo text,
    name text NOT NULL,
    status text NOT NULL,
    registration_landing_page text,
    registration_domain text,
    website_url text,
    abbreviation text,
    notification_email text,
    default_partner_ref boolean DEFAULT false,
    sflink text,
    auto_assignable boolean DEFAULT false NOT NULL,
    default_contact_id bigint,
    default_job_creator boolean DEFAULT false NOT NULL,
    source_partner boolean DEFAULT false NOT NULL,
    job_creator boolean DEFAULT false NOT NULL,
    employer_id bigint,
    redirect_partner_id bigint,
    public_api_key_hash character varying(255),
    public_api_authorities text,
    public_id character varying(22)
);


ALTER TABLE public.partner OWNER TO tctalent;

--
-- Name: partner_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.partner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.partner_id_seq OWNER TO tctalent;

--
-- Name: partner_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.partner_id_seq OWNED BY public.partner.id;


--
-- Name: partner_job; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.partner_job (
    partner_id bigint NOT NULL,
    tc_job_id bigint NOT NULL,
    contact_id bigint
);


ALTER TABLE public.partner_job OWNER TO tctalent;

--
-- Name: partner_source_country; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.partner_source_country (
    partner_id bigint NOT NULL,
    country_id bigint NOT NULL
);


ALTER TABLE public.partner_source_country OWNER TO tctalent;

--
-- Name: reaction; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.reaction (
    id bigint NOT NULL,
    chat_post_id bigint,
    emoji text
);


ALTER TABLE public.reaction OWNER TO tctalent;

--
-- Name: reaction_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.reaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reaction_id_seq OWNER TO tctalent;

--
-- Name: reaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.reaction_id_seq OWNED BY public.reaction.id;


--
-- Name: reaction_user; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.reaction_user (
    reaction_id bigint NOT NULL,
    user_id bigint NOT NULL
);


ALTER TABLE public.reaction_user OWNER TO tctalent;

--
-- Name: root_request; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.root_request (
    id bigint NOT NULL,
    ip_address text,
    partner_abbreviation text,
    query_string text,
    request_url text,
    "timestamp" timestamp without time zone,
    utm_campaign text,
    utm_content text,
    utm_medium text,
    utm_source text,
    utm_term text,
    referrer_param text
);


ALTER TABLE public.root_request OWNER TO tctalent;

--
-- Name: root_request_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.root_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.root_request_id_seq OWNER TO tctalent;

--
-- Name: root_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.root_request_id_seq OWNED BY public.root_request.id;


--
-- Name: salesforce_job_opp; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.salesforce_job_opp (
    sf_id text,
    closed boolean DEFAULT false NOT NULL,
    employer text,
    name text,
    stage text,
    stage_order integer,
    account_id text,
    owner_id text,
    id bigint NOT NULL,
    submission_due_date timestamp with time zone,
    submission_list_id bigint,
    contact_user_id bigint,
    job_summary text,
    recruiter_partner_id bigint,
    suggested_list_id bigint,
    created_by bigint,
    created_date timestamp with time zone,
    published_by bigint,
    published_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone,
    exclusion_list_id bigint,
    description text,
    salary_range text,
    location text,
    location_details text,
    benefits text,
    language_requirements text,
    employment_experience text,
    education_requirements text,
    skill_requirements text,
    occupation_code text,
    country_object_id bigint,
    job_opp_intake_id bigint,
    hiring_commitment text,
    employer_website text,
    employer_hired_internationally text,
    opportunity_score text,
    won boolean DEFAULT false NOT NULL,
    employer_description text,
    closing_comments text,
    next_step text,
    next_step_due_date date,
    employer_id bigint,
    evergreen boolean DEFAULT false NOT NULL,
    evergreen_child_id bigint,
    skip_candidate_search boolean DEFAULT false NOT NULL
);


ALTER TABLE public.salesforce_job_opp OWNER TO tctalent;

--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.salesforce_job_opp_tc_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.salesforce_job_opp_tc_job_id_seq OWNER TO tctalent;

--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.salesforce_job_opp_tc_job_id_seq OWNED BY public.salesforce_job_opp.id;


--
-- Name: saved_list; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.saved_list (
    id bigint NOT NULL,
    status text NOT NULL,
    name text NOT NULL,
    fixed boolean DEFAULT false NOT NULL,
    watcher_ids text,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    updated_by bigint,
    updated_date timestamp with time zone,
    saved_search_id bigint,
    global boolean DEFAULT false NOT NULL,
    saved_search_source_id bigint,
    displayed_fields_long text,
    displayed_fields_short text,
    folderlink text,
    foldercvlink text,
    folderjdlink text,
    registered_job boolean DEFAULT false NOT NULL,
    description text,
    published_doc_link text,
    tc_short_name text,
    sf_opp_is_closed boolean DEFAULT false NOT NULL,
    job_id bigint,
    file_jd_name text,
    file_jd_link text,
    file_joi_name text,
    file_joi_link text,
    file_interview_guidance_name text,
    file_interview_guidance_link text,
    file_mou_name text,
    file_mou_link text,
    public_id character varying(22)
);


ALTER TABLE public.saved_list OWNER TO tctalent;

--
-- Name: saved_list_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.saved_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.saved_list_id_seq OWNER TO tctalent;

--
-- Name: saved_list_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.saved_list_id_seq OWNED BY public.saved_list.id;


--
-- Name: saved_search; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.saved_search (
    id bigint NOT NULL,
    status text NOT NULL,
    name text NOT NULL,
    keyword text,
    statuses text,
    gender text,
    occupation_ids text,
    or_profile_keyword text,
    verified_occupation_ids text,
    verified_occupation_search_type text,
    nationality_ids text,
    nationality_search_type text,
    country_ids text,
    other_language_id bigint,
    un_registered boolean,
    last_modified_from date,
    last_modified_to date,
    created_from date,
    created_to date,
    min_age integer,
    max_age integer,
    education_major_ids text,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    updated_by bigint,
    updated_date timestamp with time zone,
    min_education_level integer,
    english_min_spoken_level integer,
    english_min_written_level integer,
    other_min_spoken_level integer,
    other_min_written_level integer,
    type text,
    fixed boolean DEFAULT false NOT NULL,
    reviewable boolean DEFAULT false NOT NULL,
    watcher_ids text,
    global boolean DEFAULT false NOT NULL,
    default_search boolean DEFAULT false NOT NULL,
    simple_query_string text,
    min_yrs integer,
    max_yrs integer,
    displayed_fields_long text,
    displayed_fields_short text,
    default_save_selection_list_id bigint,
    description text,
    survey_type_ids text,
    exclusion_list_id bigint,
    partner_ids text,
    job_id bigint,
    country_search_type text,
    rego_referrer_param text,
    any_opps boolean,
    closed_opps boolean,
    relocated_opps boolean,
    mini_intake_completed boolean,
    full_intake_completed boolean,
    unhcr_statuses text,
    list_all_ids text,
    list_all_search_type text,
    list_any_ids text,
    list_any_search_type text,
    potential_duplicate boolean,
    public_id character varying(22)
);


ALTER TABLE public.saved_search OWNER TO tctalent;

--
-- Name: saved_search_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.saved_search_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.saved_search_id_seq OWNER TO tctalent;

--
-- Name: saved_search_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.saved_search_id_seq OWNED BY public.saved_search.id;


--
-- Name: search_join; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.search_join (
    id bigint NOT NULL,
    search_id bigint NOT NULL,
    child_search_id bigint NOT NULL,
    search_type text NOT NULL
);


ALTER TABLE public.search_join OWNER TO tctalent;

--
-- Name: search_join_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.search_join_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.search_join_id_seq OWNER TO tctalent;

--
-- Name: search_join_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.search_join_id_seq OWNED BY public.search_join.id;


--
-- Name: shedlock; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.shedlock (
    name character varying(64) NOT NULL,
    lock_until timestamp(3) without time zone,
    locked_at timestamp(3) without time zone,
    locked_by character varying(255)
);


ALTER TABLE public.shedlock OWNER TO tctalent;

--
-- Name: survey_type; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.survey_type (
    id bigint NOT NULL,
    name text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL
);


ALTER TABLE public.survey_type OWNER TO tctalent;

--
-- Name: survey_type_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.survey_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.survey_type_id_seq OWNER TO tctalent;

--
-- Name: survey_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.survey_type_id_seq OWNED BY public.survey_type.id;


--
-- Name: system_language; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.system_language (
    id bigint NOT NULL,
    language text NOT NULL,
    label text NOT NULL,
    status text DEFAULT 'active'::text NOT NULL,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.system_language OWNER TO tctalent;

--
-- Name: system_language_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.system_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.system_language_id_seq OWNER TO tctalent;

--
-- Name: system_language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.system_language_id_seq OWNED BY public.system_language.id;


--
-- Name: task; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.task (
    id bigint NOT NULL,
    admin boolean DEFAULT false NOT NULL,
    created_by bigint NOT NULL,
    created_date timestamp with time zone NOT NULL,
    days_to_complete integer,
    description text,
    doc_link text,
    name text NOT NULL,
    optional boolean DEFAULT false NOT NULL,
    task_type text,
    updated_by bigint,
    updated_date timestamp with time zone,
    upload_subfolder_name text,
    upload_type text,
    uploadable_file_types text,
    candidate_answer_field text,
    display_name text,
    explicit_allowed_answers text
);


ALTER TABLE public.task OWNER TO tctalent;

--
-- Name: task_assignment; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.task_assignment (
    id bigint NOT NULL,
    abandoned_date timestamp with time zone,
    activated_by bigint NOT NULL,
    activated_date timestamp with time zone NOT NULL,
    candidate_id bigint NOT NULL,
    candidate_notes text,
    completed_date timestamp with time zone,
    deactivated_by bigint,
    deactivated_date timestamp with time zone,
    due_date timestamp with time zone,
    related_list_id bigint,
    status text,
    task_id bigint NOT NULL,
    task_type text
);


ALTER TABLE public.task_assignment OWNER TO tctalent;

--
-- Name: task_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.task_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_assignment_id_seq OWNER TO tctalent;

--
-- Name: task_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.task_assignment_id_seq OWNED BY public.task_assignment.id;


--
-- Name: task_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_id_seq OWNER TO tctalent;

--
-- Name: task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.task_id_seq OWNED BY public.task.id;


--
-- Name: task_saved_list; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.task_saved_list (
    task_id bigint NOT NULL,
    saved_list_id bigint NOT NULL
);


ALTER TABLE public.task_saved_list OWNER TO tctalent;

--
-- Name: translation; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.translation (
    id bigint NOT NULL,
    object_id bigint NOT NULL,
    object_type text NOT NULL,
    language text NOT NULL,
    value text NOT NULL,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone
);


ALTER TABLE public.translation OWNER TO tctalent;

--
-- Name: translation_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.translation_id_seq OWNER TO tctalent;

--
-- Name: translation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.translation_id_seq OWNED BY public.translation.id;


--
-- Name: user_job; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.user_job (
    user_id bigint NOT NULL,
    tc_job_id bigint NOT NULL
);


ALTER TABLE public.user_job OWNER TO tctalent;

--
-- Name: user_saved_list; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.user_saved_list (
    user_id bigint NOT NULL,
    saved_list_id bigint NOT NULL
);


ALTER TABLE public.user_saved_list OWNER TO tctalent;

--
-- Name: user_saved_search; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.user_saved_search (
    user_id bigint NOT NULL,
    saved_search_id bigint NOT NULL
);


ALTER TABLE public.user_saved_search OWNER TO tctalent;

--
-- Name: user_source_country; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.user_source_country (
    user_id bigint NOT NULL,
    country_id bigint NOT NULL
);


ALTER TABLE public.user_source_country OWNER TO tctalent;

--
-- Name: users; Type: TABLE; Schema: public; Owner: tctalent
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    username text,
    first_name text,
    last_name text,
    email text,
    role text NOT NULL,
    status text NOT NULL,
    password_enc text,
    last_login timestamp with time zone,
    created_by bigint,
    created_date timestamp with time zone,
    updated_by bigint,
    updated_date timestamp with time zone,
    reset_token character varying(100),
    reset_token_issued_date timestamp without time zone,
    password_updated_date timestamp without time zone,
    read_only boolean DEFAULT false NOT NULL,
    using_mfa boolean DEFAULT false NOT NULL,
    mfa_secret text,
    host_domain text,
    partner_id bigint NOT NULL,
    approver_id bigint,
    purpose text,
    job_creator boolean DEFAULT false NOT NULL,
    email_verified boolean DEFAULT false,
    email_verification_token character varying(255),
    email_verification_token_issued_time timestamp without time zone
);


ALTER TABLE public.users OWNER TO tctalent;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: tctalent
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO tctalent;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: tctalent
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- Name: candidate id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate ALTER COLUMN id SET DEFAULT nextval('public.candidate_id_seq'::regclass);


--
-- Name: candidate_attachment id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_attachment ALTER COLUMN id SET DEFAULT nextval('public.candidate_attachment_id_seq'::regclass);


--
-- Name: candidate_certification id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_certification ALTER COLUMN id SET DEFAULT nextval('public.candidate_certification_id_seq'::regclass);


--
-- Name: candidate_citizenship id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_citizenship ALTER COLUMN id SET DEFAULT nextval('public.candidate_citizenship_id_seq'::regclass);


--
-- Name: candidate_coupon_code id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_coupon_code ALTER COLUMN id SET DEFAULT nextval('public.candidate_coupon_code_id_seq'::regclass);


--
-- Name: candidate_dependant id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_dependant ALTER COLUMN id SET DEFAULT nextval('public.candidate_dependant_id_seq'::regclass);


--
-- Name: candidate_destination id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_destination ALTER COLUMN id SET DEFAULT nextval('public.candidate_destination_id_seq'::regclass);


--
-- Name: candidate_education id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_education ALTER COLUMN id SET DEFAULT nextval('public.candidate_education_id_seq'::regclass);


--
-- Name: candidate_exam id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_exam ALTER COLUMN id SET DEFAULT nextval('public.candidate_exam_id_seq'::regclass);


--
-- Name: candidate_job_experience id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_job_experience ALTER COLUMN id SET DEFAULT nextval('public.candidate_job_experience_id_seq'::regclass);


--
-- Name: candidate_language id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_language ALTER COLUMN id SET DEFAULT nextval('public.candidate_language_id_seq'::regclass);


--
-- Name: candidate_note id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_note ALTER COLUMN id SET DEFAULT nextval('public.candidate_note_id_seq'::regclass);


--
-- Name: candidate_occupation id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation ALTER COLUMN id SET DEFAULT nextval('public.candidate_occupation_id_seq'::regclass);


--
-- Name: candidate_opportunity id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_opportunity ALTER COLUMN id SET DEFAULT nextval('public.candidate_opportunity_id_seq'::regclass);


--
-- Name: candidate_review_item id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_review_item ALTER COLUMN id SET DEFAULT nextval('public.candidate_review_item_id_seq'::regclass);


--
-- Name: candidate_skill id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_skill ALTER COLUMN id SET DEFAULT nextval('public.candidate_skill_id_seq'::regclass);


--
-- Name: candidate_visa_check id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_check ALTER COLUMN id SET DEFAULT nextval('public.candidate_visa_check_id_seq'::regclass);


--
-- Name: candidate_visa_job_check id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_job_check ALTER COLUMN id SET DEFAULT nextval('public.candidate_visa_job_check_id_seq'::regclass);


--
-- Name: chat_post id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.chat_post ALTER COLUMN id SET DEFAULT nextval('public.chat_post_id_seq'::regclass);


--
-- Name: country id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.country ALTER COLUMN id SET DEFAULT nextval('public.country_id_seq'::regclass);


--
-- Name: duolingo_coupon id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_coupon ALTER COLUMN id SET DEFAULT nextval('public.duolingo_coupon_id_seq'::regclass);


--
-- Name: duolingo_extra_fields id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_extra_fields ALTER COLUMN id SET DEFAULT nextval('public.duolingo_extra_fields_id_seq'::regclass);


--
-- Name: education_level id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.education_level ALTER COLUMN id SET DEFAULT nextval('public.education_level_id_seq'::regclass);


--
-- Name: education_major id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.education_major ALTER COLUMN id SET DEFAULT nextval('public.education_major_id_seq'::regclass);


--
-- Name: employer id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.employer ALTER COLUMN id SET DEFAULT nextval('public.employer_id_seq'::regclass);


--
-- Name: export_column id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.export_column ALTER COLUMN id SET DEFAULT nextval('public.export_column_id_seq'::regclass);


--
-- Name: help_link id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.help_link ALTER COLUMN id SET DEFAULT nextval('public.help_link_id_seq'::regclass);


--
-- Name: industry id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.industry ALTER COLUMN id SET DEFAULT nextval('public.industry_id_seq'::regclass);


--
-- Name: job_chat id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat ALTER COLUMN id SET DEFAULT nextval('public.job_chat_id_seq'::regclass);


--
-- Name: job_opp_intake id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_opp_intake ALTER COLUMN id SET DEFAULT nextval('public.job_opp_intake_id_seq'::regclass);


--
-- Name: language id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.language ALTER COLUMN id SET DEFAULT nextval('public.language_id_seq'::regclass);


--
-- Name: language_level id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.language_level ALTER COLUMN id SET DEFAULT nextval('public.language_level_id_seq'::regclass);


--
-- Name: link_preview id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.link_preview ALTER COLUMN id SET DEFAULT nextval('public.link_preview_id_seq'::regclass);


--
-- Name: nationality id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.nationality ALTER COLUMN id SET DEFAULT nextval('public.nationality_id_seq'::regclass);


--
-- Name: occupation id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.occupation ALTER COLUMN id SET DEFAULT nextval('public.occupation_id_seq'::regclass);


--
-- Name: offer_to_assist id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.offer_to_assist ALTER COLUMN id SET DEFAULT nextval('public.offer_to_assist_id_seq'::regclass);


--
-- Name: partner id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner ALTER COLUMN id SET DEFAULT nextval('public.partner_id_seq'::regclass);


--
-- Name: reaction id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.reaction ALTER COLUMN id SET DEFAULT nextval('public.reaction_id_seq'::regclass);


--
-- Name: root_request id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.root_request ALTER COLUMN id SET DEFAULT nextval('public.root_request_id_seq'::regclass);


--
-- Name: salesforce_job_opp id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp ALTER COLUMN id SET DEFAULT nextval('public.salesforce_job_opp_tc_job_id_seq'::regclass);


--
-- Name: saved_list id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list ALTER COLUMN id SET DEFAULT nextval('public.saved_list_id_seq'::regclass);


--
-- Name: saved_search id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search ALTER COLUMN id SET DEFAULT nextval('public.saved_search_id_seq'::regclass);


--
-- Name: search_join id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.search_join ALTER COLUMN id SET DEFAULT nextval('public.search_join_id_seq'::regclass);


--
-- Name: survey_type id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.survey_type ALTER COLUMN id SET DEFAULT nextval('public.survey_type_id_seq'::regclass);


--
-- Name: system_language id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.system_language ALTER COLUMN id SET DEFAULT nextval('public.system_language_id_seq'::regclass);


--
-- Name: task id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task ALTER COLUMN id SET DEFAULT nextval('public.task_id_seq'::regclass);


--
-- Name: task_assignment id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment ALTER COLUMN id SET DEFAULT nextval('public.task_assignment_id_seq'::regclass);


--
-- Name: translation id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.translation ALTER COLUMN id SET DEFAULT nextval('public.translation_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: audit_log; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.audit_log (id, event_date, user_id, type, action, object_ref, description) FROM stdin;
\.


--
-- Data for Name: candidate; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate (id, candidate_number, user_id, gender, dob, phone, whatsapp, status, country_id, city, year_of_arrival, nationalityold_id, unhcr_status, unhcr_number, additional_info, max_education_level_id, created_by, created_date, updated_by, updated_date, address1, candidate_message, preferred_language, migration_status, sflink, folderlink, videolink, survey_type_id, survey_comment, text_search_id, returned_home, returned_home_reason, visa_issues, visa_issues_notes, avail_immediate, avail_immediate_reason, avail_immediate_notes, family_move, family_move_notes, int_recruit_reasons, int_recruit_rural, return_home_safe, work_permit, work_permit_desired, work_desired, asylum_year, home_location, unhcr_not_reg_status, unhcr_file, unhcr_notes, unhcr_permission, unrwa_number, unrwa_notes, dest_limit, dest_limit_notes, crime_convict, crime_convict_notes, conflict, conflict_notes, residence_status, work_abroad, host_entry_legally, left_home_notes, return_home_future, return_home_when, resettle_third, resettle_third_status, host_challenges, marital_status, partner_registered, partner_candidate_id, partner_edu_level_id, partner_occupation_id, partner_english, partner_english_level_id, partner_ielts, partner_ielts_score, partner_citizenship, military_service, visa_reject, can_drive, driving_license, driving_license_exp, driving_license_country_id, host_entry_year, english_assessment, english_assessment_score_ielts, int_recruit_rural_notes, work_abroad_notes, host_entry_legally_notes, visa_reject_notes, partner_ielts_yr, partner_edu_level_notes, partner_occupation_notes, returned_home_reason_no, residence_status_notes, work_desired_notes, left_home_reasons, military_wanted, military_notes, military_start, military_end, int_recruit_other, avail_immediate_job_ops, unhcr_registered, unrwa_registered, birth_country_id, linked_in_link, marital_status_notes, work_permit_desired_notes, host_entry_year_notes, unrwa_file, unrwa_not_reg_status, ielts_score, nationality_id, health_issues, health_issues_notes, unhcr_consent, shareable_notes, shareable_cv_attachment_id, shareable_doc_attachment_id, mini_intake, full_intake, state, external_id, external_id_source, covid_vaccinated, covid_vaccinated_status, covid_vaccinated_date, covid_vaccine_name, covid_vaccine_notes, folderlink_address, folderlink_character, folderlink_employer, folderlink_identity, folderlink_medical, folderlink_qualification, folderlink_registration, media_willingness, folderlink_engagement, folderlink_experience, folderlink_family, folderlink_immigration, folderlink_language, partner_ref, rego_ip, rego_utm_campaign, rego_utm_content, rego_utm_medium, rego_utm_source, rego_utm_term, rego_partner_param, rego_referrer_param, contact_consent_registration, contact_consent_partners, monitoring_evaluation_consent, mini_intake_completed_by, mini_intake_completed_date, full_intake_completed_by, full_intake_completed_date, french_assessment, french_assessment_score_nclc, arrest_imprison, arrest_imprison_notes, avail_date, potential_duplicate, public_id, relocated_address, relocated_city, relocated_state, relocated_country_id, english_assessment_score_det, muted, all_notifications, registered_by, change_password) FROM stdin;
\.


--
-- Data for Name: candidate_attachment; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_attachment (id, candidate_id, type, name, created_by, created_date, updated_by, updated_date, location, migrated, file_type, text_extract, cv, upload_type) FROM stdin;
\.


--
-- Data for Name: candidate_certification; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_certification (id, candidate_id, name, institution, date_completed) FROM stdin;
\.


--
-- Data for Name: candidate_citizenship; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_citizenship (id, candidate_id, nationality_id, has_passport, notes, passport_exp) FROM stdin;
\.


--
-- Data for Name: candidate_coupon_code; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_coupon_code (id, offer_to_assist_id, candidate_id, coupon_code) FROM stdin;
\.


--
-- Data for Name: candidate_dependant; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_dependant (id, candidate_id, relation, dob, health_concern, health_notes, name, registered, relation_other, registered_number, registered_notes, gender) FROM stdin;
\.


--
-- Data for Name: candidate_destination; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_destination (id, candidate_id, country_id, interest, family, location, notes) FROM stdin;
\.


--
-- Data for Name: candidate_education; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_education (id, candidate_id, education_type, country_id, length_of_course_years, institution, course_name, year_completed, major_id, incomplete) FROM stdin;
\.


--
-- Data for Name: candidate_exam; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_exam (id, candidate_id, exam, other_exam, score, year, notes) FROM stdin;
\.


--
-- Data for Name: candidate_job_experience; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_job_experience (id, candidate_id, candidate_occupation_id, company_name, country_id, role, start_date, end_date, full_time, paid, description) FROM stdin;
\.


--
-- Data for Name: candidate_language; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_language (id, candidate_id, language_id, written_level_id, spoken_level_id) FROM stdin;
\.


--
-- Data for Name: candidate_note; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_note (id, candidate_id, note_type, title, comment, created_by, created_date, updated_by, updated_date) FROM stdin;
\.


--
-- Data for Name: candidate_occupation; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_occupation (id, candidate_id, occupation_id, years_experience, top_candidate, migration_occupation, updated_by, updated_date, created_by, created_date) FROM stdin;
\.


--
-- Data for Name: candidate_opportunity; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_opportunity (id, candidate_id, closing_comments_for_candidate, employer_feedback, job_opp_id, stage, closing_comments, closed, name, next_step, next_step_due_date, sf_id, stage_order, created_by, created_date, updated_by, updated_date, won, file_offer_link, file_offer_name, last_active_stage, relocating_dependant_ids) FROM stdin;
\.


--
-- Data for Name: candidate_property; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_property (candidate_id, name, value, related_task_assignment_id) FROM stdin;
\.


--
-- Data for Name: candidate_review_item; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_review_item (id, candidate_id, saved_search_id, review_status, comment, created_by, created_date, updated_by, updated_date) FROM stdin;
\.


--
-- Data for Name: candidate_saved_list; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_saved_list (candidate_id, saved_list_id, context_note, shareable_cv_attachment_id, shareable_doc_attachment_id) FROM stdin;
\.


--
-- Data for Name: candidate_skill; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_skill (id, candidate_id, skill, time_period) FROM stdin;
\.


--
-- Data for Name: candidate_visa_check; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_visa_check (id, candidate_id, country_id, created_by, created_date, updated_by, updated_date, protection, protection_grounds, health_assessment, health_assessment_notes, character_assessment, character_assessment_notes, security_risk, security_risk_notes, valid_travel_docs, valid_travel_docs_notes, overall_risk, overall_risk_notes, english_threshold, english_threshold_notes, pathway_assessment, pathway_assessment_notes, destination_family, destination_family_location) FROM stdin;
\.


--
-- Data for Name: candidate_visa_job_check; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.candidate_visa_job_check (id, interest, interest_notes, occupation_id, occupation_notes, salary_tsmit, regional, eligible_494, eligible_494_notes, eligible_186, eligible_186_notes, eligible_other, eligible_other_notes, put_forward, notes, candidate_visa_check_id, tbb_eligibility, qualification, qualification_notes, job_opp_id, relevant_work_exp, age_requirement, preferred_pathways, ineligible_pathways, eligible_pathways, occupation_category, occupation_sub_category, english_threshold, languages_threshold_notes, languages_required, languages_threshold_met) FROM stdin;
\.


--
-- Data for Name: chat_post; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.chat_post (id, content, job_chat_id, created_by, created_date, updated_by, updated_date) FROM stdin;
\.


--
-- Data for Name: country; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.country (id, name, status, iso_code) FROM stdin;
0	Unknown	inactive	\N
\.


--
-- Data for Name: country_nationality_join; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.country_nationality_join (country_id, nationality_id) FROM stdin;
\.


--
-- Data for Name: duolingo_coupon; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.duolingo_coupon (id, coupon_code, candidate_id, expiration_date, date_sent, coupon_status, test_type) FROM stdin;
\.


--
-- Data for Name: duolingo_extra_fields; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.duolingo_extra_fields (id, certificate_url, interview_url, verification_date, percent_score, scale, literacy_subscore, conversation_subscore, comprehension_subscore, production_subscore, candidate_exam_id) FROM stdin;
\.


--
-- Data for Name: education_level; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.education_level (id, name, level, status, education_type) FROM stdin;
0	Unknown	0	inactive	\N
\.


--
-- Data for Name: education_major; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.education_major (id, name, status) FROM stdin;
0	Unknown	inactive
\.


--
-- Data for Name: employer; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.employer (id, country_id, description, has_hired_internationally, name, sf_id, website, created_by, created_date, updated_by, updated_date) FROM stdin;
\.


--
-- Data for Name: export_column; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.export_column (id, saved_list_id, saved_search_id, index, key, properties) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	init	SQL	V1__init.sql	-1077485032	tctalent	2025-06-30 16:54:33.586722	115	t
2	1.2	reset password	SQL	V1_2__reset_password.sql	140244322	tctalent	2025-06-30 16:54:33.745895	6	t
3	1.3	saved search	SQL	V1_3__saved_search.sql	1413233354	tctalent	2025-06-30 16:54:33.76874	37	t
4	1.4	address	SQL	V1_4__address.sql	576786200	tctalent	2025-06-30 16:54:33.828793	8	t
5	1.5	admin note rename	SQL	V1_5__admin_note_rename.sql	1087739736	tctalent	2025-06-30 16:54:33.851111	12	t
6	1.6	translate	SQL	V1_6__translate.sql	-1318357070	tctalent	2025-06-30 16:54:33.878625	17	t
7	1.7	shortlist	SQL	V1_7__shortlist.sql	-1251973728	tctalent	2025-06-30 16:54:33.911686	11	t
8	1.8	candidate search	SQL	V1_8__candidate_search.sql	-879535066	tctalent	2025-06-30 16:54:33.94168	15	t
9	1.9	education major	SQL	V1_9__education_major.sql	-1850452514	tctalent	2025-06-30 16:54:33.973482	5	t
10	1.10	language level	SQL	V1_10__language_level.sql	-1799099528	tctalent	2025-06-30 16:54:33.994748	24	t
11	1.11	migration changes	SQL	V1_11__migration_changes.sql	-410664132	tctalent	2025-06-30 16:54:34.044053	51	t
12	1.12	candidate file	SQL	V1_12__candidate_file.sql	-1625063091	tctalent	2025-06-30 16:54:34.110847	10	t
13	1.13	migration changes	SQL	V1_13__migration_changes.sql	-308790479	tctalent	2025-06-30 16:54:34.134034	7	t
14	1.14	migration skills	SQL	V1_14__migration_skills.sql	358145071	tctalent	2025-06-30 16:54:34.154306	8	t
15	1.15	migration seq	SQL	V1_15__migration_seq.sql	-1925857878	tctalent	2025-06-30 16:54:34.175758	21	t
16	1.16	fk fix	SQL	V1_16__fk_fix.sql	513821320	tctalent	2025-06-30 16:54:34.209617	7	t
17	1.17	additional fields	SQL	V1_17__additional_fields.sql	-1818672481	tctalent	2025-06-30 16:54:34.2327	8	t
18	1.18	missing nationality and country	SQL	V1_18__missing nationality_and_country.sql	-686353515	tctalent	2025-06-30 16:54:34.267153	14	t
19	1.19	add education type to level	SQL	V1_19__add_education_type_to_level.sql	651500969	tctalent	2025-06-30 16:54:34.30412	15	t
20	1.20	migration skills timeperiod	SQL	V1_20__migration_skills_timeperiod.sql	-2007117973	tctalent	2025-06-30 16:54:34.337566	10	t
21	1.21	candidate status	SQL	V1_21__candidate_status.sql	-1324754978	tctalent	2025-06-30 16:54:34.362987	4	t
22	1.22	extra languages	SQL	V1_22__extra_languages.sql	1754721315	tctalent	2025-06-30 16:54:34.378604	162	t
23	1.23	additional fields	SQL	V1_23__additional_fields.sql	1700856375	tctalent	2025-06-30 16:54:34.557436	13	t
24	1.24	add saved search type	SQL	V1_24__add_saved_search_type.sql	-70834252	tctalent	2025-06-30 16:54:34.585935	5	t
25	1.25	missing translations	SQL	V1_25__missing translations.sql	-1497673552	tctalent	2025-06-30 16:54:34.605109	141	t
26	1.26	preferred language	SQL	V1_26__preferred_language.sql	-811366813	tctalent	2025-06-30 16:54:34.767853	6	t
27	1.27	final data migration	SQL	V1_27__final_data_migration.sql	318882810	tctalent	2025-06-30 16:54:34.795158	9	t
28	1.28	remove CN prefix from candidate numbers	SQL	V1_28__remove_CN_prefix_from_candidate_numbers.sql	207213744	tctalent	2025-06-30 16:54:34.820665	4	t
29	1.29	add salesforce and folder link fields	SQL	V1_29__add_salesforce_and_folder_link_fields.sql	1040992300	tctalent	2025-06-30 16:54:34.836381	6	t
30	1.30	delete salesforce and google links from attachments	SQL	V1_30__delete_salesforce_and_google_links_from_attachments.sql	-134777281	tctalent	2025-06-30 16:54:34.854426	3	t
31	1.31	delete unused table shortlist candidate	SQL	V1_31__delete_unused_table_shortlist_candidate.sql	-517813501	tctalent	2025-06-30 16:54:34.868929	4	t
32	1.32	add salesforce and folder link fields	SQL	V1_32__add_salesforce_and_folder_link_fields.sql	421433909	tctalent	2025-06-30 16:54:34.88543	5	t
33	1.33	add saved search fixed and reviewable	SQL	V1_33__add_saved_search_fixed_and_reviewable.sql	612297856	tctalent	2025-06-30 16:54:34.902454	6	t
34	1.34	add candidate number index	SQL	V1_34__add_candidate_number_index.sql	1260635709	tctalent	2025-06-30 16:54:34.920975	6	t
35	1.35	merge duplicate nationality translations	SQL	V1_35__merge_duplicate_nationality_translations.sql	-1858875527	tctalent	2025-06-30 16:54:34.939346	13	t
36	1.36	add  user saved search table	SQL	V1_36__add__user_saved_search_table.sql	-210273505	tctalent	2025-06-30 16:54:34.964027	8	t
37	1.37	add candidate survey	SQL	V1_37__add_candidate_survey.sql	-1427020279	tctalent	2025-06-30 16:54:34.988417	37	t
38	1.38	add read only to user	SQL	V1_38__add_read_only_to_user.sql	-1565011072	tctalent	2025-06-30 16:54:35.045562	7	t
39	1.39	add saved search watchers	SQL	V1_39__add_saved_search_watchers.sql	477592790	tctalent	2025-06-30 16:54:35.074182	8	t
40	1.40	add user source country table	SQL	V1_40__add_user_source_country_table.sql	947324760	tctalent	2025-06-30 16:54:35.096734	7	t
41	1.41	delete registered fields from candidate	SQL	V1_41__delete_registered_fields_from_candidate.sql	1696726601	tctalent	2025-06-30 16:54:35.119349	4	t
42	1.42	list	SQL	V1_42__list.sql	1150362316	tctalent	2025-06-30 16:54:35.136644	21	t
43	1.43	add text extract	SQL	V1_43__add_text_extract.sql	-1330293645	tctalent	2025-06-30 16:54:35.17135	4	t
44	1.44	convert date completed to date	SQL	V1_44__convert_date_completed_to_date.sql	-245669521	tctalent	2025-06-30 16:54:35.187412	8	t
45	1.45	add cv to attachment	SQL	V1_45__add_cv_to_attachment.sql	-1217777125	tctalent	2025-06-30 16:54:35.211405	8	t
46	1.46	add saved search to saved list	SQL	V1_46__add_saved_search_to_saved_list.sql	-933336748	tctalent	2025-06-30 16:54:35.238298	5	t
47	1.47	add global flag to candidate source	SQL	V1_47__add_global_flag_to_candidate_source.sql	-1917745717	tctalent	2025-06-30 16:54:35.263372	8	t
48	1.48	add default flag to saved search	SQL	V1_48__add_default_flag_to_saved_search.sql	1154680281	tctalent	2025-06-30 16:54:35.289491	3	t
49	1.49	rename shortlist to review	SQL	V1_49__rename_shortlist_to_review.sql	2002095591	tctalent	2025-06-30 16:54:35.30707	5	t
50	1.50	rename shortlist to review 2	SQL	V1_50__rename_shortlist_to_review_2.sql	-855995288	tctalent	2025-06-30 16:54:35.32382	7	t
51	1.51	add simple query string search	SQL	V1_51__add_simple_query_string_search.sql	-2056455198	tctalent	2025-06-30 16:54:35.342768	4	t
52	1.52	add candidate text search id	SQL	V1_52__add_candidate_text_search_id.sql	-1364621597	tctalent	2025-06-30 16:54:35.358699	4	t
53	1.53	add years exp to saved search	SQL	V1_53__add_years_exp_to_saved_search.sql	1133516610	tctalent	2025-06-30 16:54:35.374079	4	t
54	1.54	add sfJoblink	SQL	V1_54__add_sfJoblink.sql	1884228486	tctalent	2025-06-30 16:54:35.390255	4	t
55	1.55	remove admin only cand attachment	SQL	V1_55__remove_admin_only_cand_attachment.sql	-1290077746	tctalent	2025-06-30 16:54:35.406109	3	t
56	1.56	add context note	SQL	V1_56__add_context_note.sql	-1319514039	tctalent	2025-06-30 16:54:35.421254	13	t
57	1.57	add returned home fields	SQL	V1_57__add_returned_home_fields.sql	-987458724	tctalent	2025-06-30 16:54:35.449456	7	t
58	1.58	add visaIssues	SQL	V1_58__add_visaIssues.sql	-1591287910	tctalent	2025-06-30 16:54:35.479096	8	t
59	1.59	change returned home type	SQL	V1_59__change_returned_home_type.sql	1405733381	tctalent	2025-06-30 16:54:35.510603	25	t
60	1.60	add availability family fields	SQL	V1_60__add_availability_family_fields.sql	846044640	tctalent	2025-06-30 16:54:35.553357	10	t
61	1.61	add candidate citizenship table	SQL	V1_61__add_candidate_citizenship_table.sql	571970646	tctalent	2025-06-30 16:54:35.577698	8	t
62	1.62	add int recruit fields	SQL	V1_62__add_int_recruit_fields.sql	1146491471	tctalent	2025-06-30 16:54:35.599734	4	t
63	1.63	add home safe fields	SQL	V1_63__add_home_safe_fields.sql	-2052452041	tctalent	2025-06-30 16:54:35.615598	3	t
64	1.64	add work permit	SQL	V1_64__add_work_permit.sql	261278103	tctalent	2025-06-30 16:54:35.631378	5	t
65	1.65	add work permit desired	SQL	V1_65__add_work_permit_desired.sql	15658870	tctalent	2025-06-30 16:54:35.657593	10	t
66	1.66	add intake form fields	SQL	V1_66__add_intake_form_fields.sql	-1089637251	tctalent	2025-06-30 16:54:35.691175	13	t
67	1.67	intake form fields two	SQL	V1_67__intake_form_fields_two.sql	1552752546	tctalent	2025-06-30 16:54:35.725603	13	t
68	1.68	candidate destination table	SQL	V1_68__candidate_destination_table.sql	-1695959639	tctalent	2025-06-30 16:54:35.750962	8	t
69	1.69	add candidate visa table	SQL	V1_69__add_candidate_visa_table.sql	-754178328	tctalent	2025-06-30 16:54:35.771697	10	t
70	1.70	visa-check fields	SQL	V1_70__visa-check_fields.sql	-1606232815	tctalent	2025-06-30 16:54:35.794436	8	t
71	1.71	add cand exam table	SQL	V1_71__add_cand_exam_table.sql	-997983609	tctalent	2025-06-30 16:54:35.814668	7	t
72	1.72	add dest limit fields	SQL	V1_72__add_dest_limit_fields.sql	1442217040	tctalent	2025-06-30 16:54:35.835128	4	t
73	1.73	add dest job fields	SQL	V1_73__add_dest_job_fields.sql	-226367207	tctalent	2025-06-30 16:54:35.85369	6	t
74	1.74	add full intake fields	SQL	V1_74__add_full_intake_fields.sql	-1107228319	tctalent	2025-06-30 16:54:35.881075	26	t
75	1.75	add work abroad fields	SQL	V1_75__add_work_abroad_fields.sql	-1463525226	tctalent	2025-06-30 16:54:35.927746	8	t
76	1.76	add saved search source to saved list	SQL	V1_76__add_saved_search_source_to_saved_list.sql	-1227081362	tctalent	2025-06-30 16:54:35.947972	4	t
77	1.77	set saved search source id on all selectionlists	SQL	V1_77__set_saved_search_source_id_on_all_selectionlists.sql	-1818982527	tctalent	2025-06-30 16:54:35.966417	4	t
78	1.78	add partner fields	SQL	V1_78__add_partner_fields.sql	1797441687	tctalent	2025-06-30 16:54:35.981335	11	t
79	1.79	add children fields	SQL	V1_79__add_children_fields.sql	1018271994	tctalent	2025-06-30 16:54:36.005729	5	t
80	1.80	add visa reject field	SQL	V1_80__add_visa_reject_field.sql	1110674192	tctalent	2025-06-30 16:54:36.022941	4	t
81	1.81	add host born field	SQL	V1_81__add_host_born_field.sql	-2111273938	tctalent	2025-06-30 16:54:36.03909	4	t
82	1.82	add driving license fields	SQL	V1_82__add_driving_license_fields.sql	-592118483	tctalent	2025-06-30 16:54:36.057357	11	t
83	1.83	add dependants fields	SQL	V1_83__add_dependants_fields.sql	-110734141	tctalent	2025-06-30 16:54:36.085313	8	t
84	1.84	add displayed fields	SQL	V1_84__add_displayed_fields.sql	1401063973	tctalent	2025-06-30 16:54:36.116309	9	t
85	1.85	rename un fields	SQL	V1_85__rename_un_fields.sql	-579227362	tctalent	2025-06-30 16:54:36.144568	23	t
86	1.86	new intake fields	SQL	V1_86__new_intake_fields.sql	-1143334246	tctalent	2025-06-30 16:54:36.18208	6	t
87	1.87	add dependants table	SQL	V1_87__add_dependants_table.sql	171034442	tctalent	2025-06-30 16:54:36.199844	9	t
88	1.88	add lang assessment fields	SQL	V1_88__add_lang_assessment_fields.sql	-86887548	tctalent	2025-06-30 16:54:36.221651	4	t
89	1.89	remove unused intake fields	SQL	V1_89__remove_unused_intake_fields.sql	-242118645	tctalent	2025-06-30 16:54:36.237371	9	t
90	1.90	intake fields feedback	SQL	V1_90__intake_fields_feedback.sql	-427644251	tctalent	2025-06-30 16:54:36.259597	12	t
91	1.91	drop work abroad loc id	SQL	V1_91__drop_work_abroad_loc_id.sql	1636402555	tctalent	2025-06-30 16:54:36.290536	9	t
92	1.92	intake feedback	SQL	V1_92__intake_feedback.sql	-621087515	tctalent	2025-06-30 16:54:36.327317	27	t
93	1.93	reset partner ielts	SQL	V1_93__reset_partner_ielts.sql	-993773149	tctalent	2025-06-30 16:54:36.374255	5	t
94	1.94	relation enum parent	SQL	V1_94__relation_enum_parent.sql	1038765772	tctalent	2025-06-30 16:54:36.392756	5	t
95	1.95	cand dest parent enum	SQL	V1_95__cand_dest_parent_enum.sql	1426790366	tctalent	2025-06-30 16:54:36.410495	5	t
96	1.96	add birth country	SQL	V1_96__add_birth_country.sql	-1120400510	tctalent	2025-06-30 16:54:36.426294	4	t
97	1.97	add linkedin	SQL	V1_97__add_linkedin.sql	515412765	tctalent	2025-06-30 16:54:36.443027	4	t
98	1.98	rename linkedin	SQL	V1_98__rename_linkedin.sql	207053087	tctalent	2025-06-30 16:54:36.459279	4	t
99	1.99	drop draft deleted	SQL	V1_99__drop_draft_deleted.sql	-2055426319	tctalent	2025-06-30 16:54:36.475164	6	t
100	1.100	add visa fields	SQL	V1_100__add_visa_fields.sql	1897650333	tctalent	2025-06-30 16:54:36.4967	25	t
101	1.101	rename visa fields	SQL	V1_101__rename_visa_fields.sql	-2021084215	tctalent	2025-06-30 16:54:36.546499	8	t
102	1.102	add users mfa	SQL	V1_102__add_users_mfa.sql	-1815035015	tctalent	2025-06-30 16:54:36.572419	5	t
103	1.103	add candidate status ineligible rename inactive	SQL	V1_103__add_candidate_status_ineligible_rename_inactive.sql	694437288	tctalent	2025-06-30 16:54:36.591179	3	t
104	1.104	RenameReviewStatus pending to unverified	SQL	V1_104__RenameReviewStatus_pending_to_unverified.sql	-711050923	tctalent	2025-06-30 16:54:36.604997	3	t
105	1.105	rename visa tables	SQL	V1_105__rename_visa_tables.sql	-257733220	tctalent	2025-06-30 16:54:36.617203	7	t
106	1.106	rename visa sequence	SQL	V1_106__rename_visa_sequence.sql	1835657645	tctalent	2025-06-30 16:54:36.637065	3	t
107	1.107	visa job tbb eligible	SQL	V1_107__visa_job_tbb_eligible.sql	-491582417	tctalent	2025-06-30 16:54:36.651383	4	t
108	1.108	rename visa check seq	SQL	V1_108__rename_visa_check_seq.sql	882275282	tctalent	2025-06-30 16:54:36.668141	3	t
109	1.109	fix enum visa issues	SQL	V1_109__fix_enum_visa_issues.sql	727466319	tctalent	2025-06-30 16:54:36.682424	6	t
110	1.110	add name visa job	SQL	V1_110__add_name_visa_job.sql	1515280728	tctalent	2025-06-30 16:54:36.710516	7	t
111	1.111	update visa job fields	SQL	V1_111__update_visa_job_fields.sql	1807759813	tctalent	2025-06-30 16:54:36.74232	9	t
112	1.112	add unhcr survey type	SQL	V1_112__add_unhcr_survey_type.sql	-1801807781	tctalent	2025-06-30 16:54:36.768499	6	t
113	1.113	add default save selection list id	SQL	V1_113__add_default_save_selection_list_id.sql	1955489579	tctalent	2025-06-30 16:54:36.799395	4	t
114	1.114	intake feedback update	SQL	V1_114__intake_feedback_update.sql	1727420106	tctalent	2025-06-30 16:54:36.822518	4	t
115	1.115	intake feedback fields	SQL	V1_115__intake_feedback_fields.sql	1678696356	tctalent	2025-06-30 16:54:36.838428	8	t
116	1.116	intake registration	SQL	V1_116__intake_registration.sql	1895494422	tctalent	2025-06-30 16:54:36.857616	7	t
117	1.117	add host entry	SQL	V1_117__add_host_entry.sql	567462405	tctalent	2025-06-30 16:54:36.877315	4	t
118	1.118	add unrwa file	SQL	V1_118__add_unrwa_file.sql	280899826	tctalent	2025-06-30 16:54:36.893002	6	t
119	1.119	add ielts sort	SQL	V1_119__add_ielts_sort.sql	1950368445	tctalent	2025-06-30 16:54:36.913211	7	t
120	1.120	intake feedback june	SQL	V1_120__intake_feedback_june.sql	-2132480187	tctalent	2025-06-30 16:54:36.942144	18	t
121	1.121	fix bug in flyway	SQL	V1_121__fix_bug_in_flyway.sql	2090101478	tctalent	2025-06-30 16:54:36.983254	5	t
122	1.122	create nationality country join table	SQL	V1_122__create_nationality_country_join_table.sql	2041110357	tctalent	2025-06-30 16:54:37.008205	8	t
123	1.123	update nationality to country	SQL	V1_123__update_nationality_to_country.sql	-776027489	tctalent	2025-06-30 16:54:37.02815	10	t
124	1.124	add savedlist folder link	SQL	V1_124__add_savedlist_folder_link.sql	872647122	tctalent	2025-06-30 16:54:37.054108	4	t
125	1.125	add health issues intake	SQL	V1_125__add_health_issues_intake.sql	943221668	tctalent	2025-06-30 16:54:37.071668	4	t
126	1.126	add num dependants	SQL	V1_126__add_num_dependants.sql	-70001797	tctalent	2025-06-30 16:54:37.088677	4	t
127	1.127	add more savedlist folder links	SQL	V1_127__add_more_savedlist_folder_links.sql	-177300958	tctalent	2025-06-30 16:54:37.105561	7	t
128	1.128	add savedlist registered	SQL	V1_128__add_savedlist_registered.sql	922780386	tctalent	2025-06-30 16:54:37.129744	5	t
129	1.129	remove num dependants	SQL	V1_129__remove_num_dependants.sql	206858861	tctalent	2025-06-30 16:54:37.157222	9	t
130	1.130	add unhcr consent	SQL	V1_130__add_unhcr_consent.sql	1493594122	tctalent	2025-06-30 16:54:37.194671	9	t
131	1.131	replace null max education with 0	SQL	V1_131__replace_null_max_education_with_0.sql	864095392	tctalent	2025-06-30 16:54:37.223839	6	t
132	1.132	rename left home notes	SQL	V1_132__rename_left_home_notes.sql	-385201356	tctalent	2025-06-30 16:54:37.247448	6	t
133	1.133	add source export fields and description	SQL	V1_133__add_source_export_fields_and_description.sql	-1640411199	tctalent	2025-06-30 16:54:37.272596	9	t
134	1.134	add candidate shareable fields	SQL	V1_134__add_candidate_shareable_fields.sql	-200295520	tctalent	2025-06-30 16:54:37.297128	8	t
135	1.135	add intake flags	SQL	V1_135__add_intake_flags.sql	2102589696	tctalent	2025-06-30 16:54:37.320669	9	t
136	1.136	add save list published doc link	SQL	V1_136__add_save_list_published_doc_link.sql	-1021957176	tctalent	2025-06-30 16:54:37.348357	6	t
137	1.137	add us afghan survey	SQL	V1_137__add_us_afghan_survey.sql	1006981261	tctalent	2025-06-30 16:54:37.379118	6	t
138	1.138	rename source export fields to columns	SQL	V1_138__rename_source_export_fields_to_columns.sql	-1904406307	tctalent	2025-06-30 16:54:37.405834	8	t
139	1.139	create export column table	SQL	V1_139__create_export_column_table.sql	1275237479	tctalent	2025-06-30 16:54:37.430444	11	t
140	1.140	add state	SQL	V1_140__add_state.sql	-2065476680	tctalent	2025-06-30 16:54:37.454434	4	t
141	1.141	add external ids	SQL	V1_141__add_external_ids.sql	-216641303	tctalent	2025-06-30 16:54:37.471797	4	t
142	1.142	add external id index	SQL	V1_142__add_external_id_index.sql	-2068647974	tctalent	2025-06-30 16:54:37.489118	6	t
143	1.143	trim first last names	SQL	V1_143__trim_first_last_names.sql	-18806076	tctalent	2025-06-30 16:54:37.509141	4	t
144	1.144	add survey type saved search	SQL	V1_144__add_survey_type_saved_search.sql	-850356437	tctalent	2025-06-30 16:54:37.526622	5	t
145	1.145	max educate level not null	SQL	V1_145__max_educate_level_not_null.sql	864095392	tctalent	2025-06-30 16:54:37.545055	6	t
146	1.146	add exclusion list saved search	SQL	V1_146__add_exclusion_list_saved_search.sql	-1444452253	tctalent	2025-06-30 16:54:37.572012	8	t
147	1.147	shareable docs saved list	SQL	V1_147__shareable_docs_saved_list.sql	932083738	tctalent	2025-06-30 16:54:37.597385	7	t
148	1.148	add isco to occupation	SQL	V1_148__add_isco_to_occupation.sql	-49646765	tctalent	2025-06-30 16:54:37.62746	5	t
149	1.149	add new occupations	SQL	V1_149__add_new_occupations.sql	2102205744	tctalent	2025-06-30 16:54:37.645092	18	t
150	1.150	add isco to existing occupations	SQL	V1_150__add_isco_to_existing_occupations.sql	1103085489	tctalent	2025-06-30 16:54:37.67546	160	t
151	1.151	rename existing occupations	SQL	V1_151__rename_existing_occupations.sql	614019826	tctalent	2025-06-30 16:54:37.8508	14	t
152	1.152	add covid vaccine fields	SQL	V1_152__add_covid_vaccine_fields.sql	1944570438	tctalent	2025-06-30 16:54:37.877416	8	t
153	1.153	add tbb short name	SQL	V1_153__add_tbb_short_name.sql	1422673303	tctalent	2025-06-30 16:54:37.897714	5	t
154	1.154	add fa dari language	SQL	V1_154__add_fa_dari_language.sql	-1946383290	tctalent	2025-06-30 16:54:37.916026	3	t
155	1.155	alter visa job qualification	SQL	V1_155__alter_visa_job_qualification.sql	-1077838290	tctalent	2025-06-30 16:54:37.930713	5	t
156	1.156	add visa intake fields	SQL	V1_156__add_visa_intake_fields.sql	-1180716424	tctalent	2025-06-30 16:54:37.946652	5	t
157	1.157	add iso codes	SQL	V1_157__add_iso_codes.sql	1019988719	tctalent	2025-06-30 16:54:37.963626	4	t
158	1.158	update country names	SQL	V1_158__update_country_names.sql	558634287	tctalent	2025-06-30 16:54:37.978679	12	t
159	1.159	update language names	SQL	V1_159__update_language_names.sql	586614066	tctalent	2025-06-30 16:54:38.002941	3	t
160	1.160	remove external id unique index	SQL	V1_160__remove_external_id_unique_index.sql	-331616821	tctalent	2025-06-30 16:54:38.017485	3	t
161	1.161	update visa job qualification	SQL	V1_161__update_visa_job_qualification.sql	-1677643939	tctalent	2025-06-30 16:54:38.032422	3	t
162	1.162	add new survey types	SQL	V1_162__add_new_survey_types.sql	-569232089	tctalent	2025-06-30 16:54:38.045608	4	t
163	1.163	add user host domain	SQL	V1_163__add_user_host_domain.sql	702009388	tctalent	2025-06-30 16:54:38.06028	5	t
164	1.164	create task and taskassignment tables	SQL	V1_164__create_task_and_taskassignment_tables.sql	-1906586642	tctalent	2025-06-30 16:54:38.078582	28	t
165	1.165	add candidate attachment upload type	SQL	V1_165__add_candidate_attachment_upload_type.sql	-148773548	tctalent	2025-06-30 16:54:38.127134	11	t
166	1.166	add standard tasks	SQL	V1_166__add_standard_tasks.sql	1390736373	tctalent	2025-06-30 16:54:38.158909	13	t
167	1.167	add more standard tasks	SQL	V1_167__add_more_standard_tasks.sql	-1429283740	tctalent	2025-06-30 16:54:38.186767	23	t
168	1.168	candidate folder links	SQL	V1_168__candidate_folder_links.sql	-1319397545	tctalent	2025-06-30 16:54:38.224288	8	t
169	1.169	remove empystring folderlinks	SQL	V1_169__remove_empystring_folderlinks.sql	-1540122495	tctalent	2025-06-30 16:54:38.244024	4	t
170	1.170	add gender dependants	SQL	V1_170__add_gender_dependants.sql	1120903965	tctalent	2025-06-30 16:54:38.25865	4	t
171	1.171	add techfugees survey	SQL	V1_171__add_techfugees_survey.sql	351287120	tctalent	2025-06-30 16:54:38.275246	6	t
172	1.172	add intake upload tasks	SQL	V1_172__add_intake_upload_tasks.sql	-1444202797	tctalent	2025-06-30 16:54:38.295051	8	t
173	1.173	add task saved list	SQL	V1_173__add_task_saved_list.sql	-573038287	tctalent	2025-06-30 16:54:38.32434	12	t
174	1.174	add media willingness	SQL	V1_174__add_media_willingness.sql	1866224105	tctalent	2025-06-30 16:54:38.353928	3	t
175	1.175	update task help links	SQL	V1_175__update_task_help_links.sql	480697816	tctalent	2025-06-30 16:54:38.371622	4	t
176	1.176	more candidate folder links	SQL	V1_176__more_candidate_folder_links.sql	822290005	tctalent	2025-06-30 16:54:38.387323	6	t
177	1.177	update task upload dir	SQL	V1_177__update_task_upload_dir.sql	-1100684422	tctalent	2025-06-30 16:54:38.405465	5	t
178	1.178	new tasks	SQL	V1_178__new_tasks.sql	484862755	tctalent	2025-06-30 16:54:38.421735	6	t
179	1.179	delete null language id	SQL	V1_179__delete_null_language_id.sql	-1734034610	tctalent	2025-06-30 16:54:38.441251	3	t
180	1.180	candidate property	SQL	V1_180__candidate_property.sql	-1606220888	tctalent	2025-06-30 16:54:38.455009	9	t
181	1.181	task candidate answer field	SQL	V1_181__task_candidate_answer_field.sql	-1583723783	tctalent	2025-06-30 16:54:38.477324	4	t
182	1.182	remove subtasks	SQL	V1_182__remove_subtasks.sql	-1861560633	tctalent	2025-06-30 16:54:38.49408	6	t
183	1.183	candidate property task assignment	SQL	V1_183__candidate_property_task_assignment.sql	-1129646197	tctalent	2025-06-30 16:54:38.518209	7	t
184	1.184	task display name	SQL	V1_184__task_display_name.sql	-646443241	tctalent	2025-06-30 16:54:38.544499	9	t
185	1.185	task change name to upload type	SQL	V1_185__task_change_name_to_upload_type.sql	-1843013369	tctalent	2025-06-30 16:54:38.576445	6	t
186	1.186	add task assignment task type	SQL	V1_186__add_task_assignment_task_type.sql	1570772248	tctalent	2025-06-30 16:54:38.596162	5	t
187	1.187	init task assignment task type values	SQL	V1_187__init_task_assignment_task_type_values.sql	-687294010	tctalent	2025-06-30 16:54:38.61369	5	t
188	1.188	add other task types	SQL	V1_188__add_other_task_types.sql	2145326903	tctalent	2025-06-30 16:54:38.630595	6	t
189	1.189	update visa returned task	SQL	V1_189__update_visa_returned_task.sql	557656958	tctalent	2025-06-30 16:54:38.648697	3	t
190	1.190	predeparture video task	SQL	V1_190__predeparture_video_task.sql	-2097212481	tctalent	2025-06-30 16:54:38.663325	3	t
191	1.191	add apc form task	SQL	V1_191__add_apc_form_task.sql	-1685849378	tctalent	2025-06-30 16:54:38.678278	4	t
192	1.192	add oet tasks	SQL	V1_192__add_oet_tasks.sql	-570046749	tctalent	2025-06-30 16:54:38.694655	13	t
193	1.193	saved list add sf opp is closed	SQL	V1_193__saved_list_add_sf_opp_is_closed.sql	345843220	tctalent	2025-06-30 16:54:38.731399	7	t
194	1.194	saved list registered job is global	SQL	V1_194__saved_list_registered_job_is_global.sql	-319764210	tctalent	2025-06-30 16:54:38.766191	4	t
195	1.195	add explicit allowed answers	SQL	V1_195__add_explicit_allowed_answers.sql	-1917218375	tctalent	2025-06-30 16:54:38.793347	8	t
196	1.196	add questionaire tasks	SQL	V1_196__add_questionaire_tasks.sql	-1110409461	tctalent	2025-06-30 16:54:38.814019	9	t
197	1.197	add more questionaire tasks	SQL	V1_197__add_more_questionaire_tasks.sql	113951848	tctalent	2025-06-30 16:54:38.835449	10	t
198	1.198	add language level	SQL	V1_198__add_language_level.sql	855235939	tctalent	2025-06-30 16:54:38.857742	3	t
199	1.199	add simple task	SQL	V1_199__add_simple_task.sql	-831397700	tctalent	2025-06-30 16:54:38.872478	3	t
200	1.200	add partner source country table	SQL	V1_200__add_partner_source_country_table.sql	966886918	tctalent	2025-06-30 16:54:38.887996	15	t
201	1.201	add other partners	SQL	V1_201__add_other_partners.sql	1752320312	tctalent	2025-06-30 16:54:38.916028	7	t
202	1.202	add partner source country table	SQL	V1_202__add_partner_source_country_table.sql	1708323621	tctalent	2025-06-30 16:54:38.941502	9	t
203	1.203	update system admin user to new systemadmin role	SQL	V1_203__update_system_admin_user_to_new_systemadmin_role.sql	1285821041	tctalent	2025-06-30 16:54:38.964827	5	t
204	1.204	add partner abbreviation	SQL	V1_204__add_partner_abbreviation.sql	-2092216792	tctalent	2025-06-30 16:54:38.994828	7	t
205	1.205	rename to registration domain	SQL	V1_205__rename_to_registration_domain.sql	-698879107	tctalent	2025-06-30 16:54:39.017059	3	t
206	1.206	add crs and dignity partners	SQL	V1_206__add_crs_and_dignity_partners.sql	-1853998048	tctalent	2025-06-30 16:54:39.033891	4	t
207	1.207	add candidate partner ref	SQL	V1_207__add_candidate_partner_ref.sql	253731004	tctalent	2025-06-30 16:54:39.048384	7	t
208	1.208	add partners to saved search	SQL	V1_208__add_partners_to_saved_search.sql	-1115316962	tctalent	2025-06-30 16:54:39.066917	3	t
209	1.209	add partners abbreviations	SQL	V1_209__add_partners_abbreviations.sql	-1500786289	tctalent	2025-06-30 16:54:39.081208	6	t
210	1.210	set partners abbreviations not null	SQL	V1_210__set_partners_abbreviations_not_null.sql	53510123	tctalent	2025-06-30 16:54:39.098382	2	t
211	1.211	add afghantalent partner	SQL	V1_211__add_afghantalent_partner.sql	-234198999	tctalent	2025-06-30 16:54:39.110396	3	t
212	1.212	update unhcrstatus from registered	SQL	V1_212__update_unhcrstatus_from_registered.sql	36671653	tctalent	2025-06-30 16:54:39.124132	5	t
213	1.213	replace null unhcrstatus with no response	SQL	V1_213__replace_null_unhcrstatus_with_no_response.sql	-1651712130	tctalent	2025-06-30 16:54:39.141154	7	t
214	1.214	add partner sflink	SQL	V1_214__add_partner_sflink.sql	811234528	tctalent	2025-06-30 16:54:39.16295	7	t
215	1.215	add nmc prn task	SQL	V1_215__add_nmc_prn_task.sql	-785974895	tctalent	2025-06-30 16:54:39.191486	9	t
216	1.216	add Job and SalesforceJobOpp tables	SQL	V1_216__add_Job_and_SalesforceJobOpp_tables.sql	-1591142597	tctalent	2025-06-30 16:54:39.216324	20	t
217	1.217	add SalesforceJobOpp link to Job	SQL	V1_217__add_SalesforceJobOpp_link_to_Job.sql	1948152190	tctalent	2025-06-30 16:54:39.258673	7	t
218	1.218	replace Job link with SalesforceJobOpp link	SQL	V1_218__replace_Job_link_with_SalesforceJobOpp_link.sql	1088972585	tctalent	2025-06-30 16:54:39.277699	7	t
219	1.219	drop job sf job opp id	SQL	V1_219__drop_job_sf_job_opp_id.sql	-1861610563	tctalent	2025-06-30 16:54:39.296492	4	t
220	1.220	create root request table	SQL	V1_220__create_root_request_table.sql	-2069787225	tctalent	2025-06-30 16:54:39.312167	10	t
221	1.221	add candidate rego fields	SQL	V1_221__add_candidate_rego_fields.sql	-980774307	tctalent	2025-06-30 16:54:39.337909	13	t
222	1.222	add auto assignable	SQL	V1_222__add_auto_assignable.sql	-115530419	tctalent	2025-06-30 16:54:39.371092	6	t
223	1.223	add new candidate rego fields	SQL	V1_223__add_new_candidate_rego_fields.sql	-1495674108	tctalent	2025-06-30 16:54:39.395031	9	t
224	1.224	add SalesforceJobOpp accountId ownerId	SQL	V1_224__add_SalesforceJobOpp_accountId_ownerId.sql	-49952322	tctalent	2025-06-30 16:54:39.426282	8	t
225	1.225	drop candidate source sf joblink	SQL	V1_225__drop_candidate_source_sf_joblink.sql	-1409576158	tctalent	2025-06-30 16:54:39.451226	5	t
226	1.226	rename stage visaGranted to hiringCompleted	SQL	V1_226__rename_stage_visaGranted_to_hiringCompleted.sql	-249227355	tctalent	2025-06-30 16:54:39.470371	5	t
227	1.227	rename employer ineligible stage	SQL	V1_227__rename_employer_ineligible_stage.sql	1595736101	tctalent	2025-06-30 16:54:39.487527	4	t
228	1.228	job fields to salesforce job opp	SQL	V1_228__job_fields_to_salesforce_job_opp.sql	1275742817	tctalent	2025-06-30 16:54:39.502716	11	t
229	1.229	copy submission list ids from job	SQL	V1_229__copy_submission_list_ids_from_job.sql	940641600	tctalent	2025-06-30 16:54:39.527186	22	t
230	1.230	new salesforce job fields	SQL	V1_230__new_salesforce_job_fields.sql	440663533	tctalent	2025-06-30 16:54:39.564439	53	t
231	1.231	more job fields to salesforce job opp	SQL	V1_231__more_job_fields_to_salesforce_job_opp.sql	276973997	tctalent	2025-06-30 16:54:39.639966	11	t
232	1.232	still more job fields to salesforce job opp	SQL	V1_232__still_more_job_fields_to_salesforce_job_opp.sql	-562160571	tctalent	2025-06-30 16:54:39.663175	6	t
233	1.233	add jd and joi file links to saved list	SQL	V1_233__add_jd_and_joi_file_links_to_saved_list.sql	1433267662	tctalent	2025-06-30 16:54:39.679883	7	t
234	1.234	add job source partner	SQL	V1_234__add_job_source_partner.sql	2033599700	tctalent	2025-06-30 16:54:39.706859	10	t
235	1.235	add default contact to partner	SQL	V1_235__add_default_contact_to_partner.sql	1506536567	tctalent	2025-06-30 16:54:39.732361	4	t
236	1.236	add job starring	SQL	V1_236__add_job_starring.sql	1144032102	tctalent	2025-06-30 16:54:39.749081	9	t
237	1.237	add job intake fields	SQL	V1_237__add_job_intake_fields.sql	-2045468119	tctalent	2025-06-30 16:54:39.77072	4	t
238	1.238	initialize job accepting and published fields	SQL	V1_238__initialize_job_accepting_and_published_fields.sql	62225677	tctalent	2025-06-30 16:54:39.789228	4	t
239	1.239	initialize job created fields	SQL	V1_239__initialize_job_created_fields.sql	1110696165	tctalent	2025-06-30 16:54:39.811508	9	t
240	1.240	add country search type to saved search	SQL	V1_240__add_country_search_type_to_saved_search.sql	972607621	tctalent	2025-06-30 16:54:39.840613	5	t
241	1.241	add rego referrer param to saved search	SQL	V1_241__add_rego_referrer_param_to_saved_search.sql	-1536940874	tctalent	2025-06-30 16:54:39.860405	5	t
242	1.242	add index on candidate status	SQL	V1_242__add_index_on_candidate_status.sql	178856824	tctalent	2025-06-30 16:54:39.877186	8	t
243	1.243	remove unused job table	SQL	V1_243__remove_unused_job_table.sql	-1651528189	tctalent	2025-06-30 16:54:39.897169	4	t
244	1.244	add salary range job	SQL	V1_244__add_salary_range_job.sql	1881407372	tctalent	2025-06-30 16:54:39.913462	3	t
245	1.245	add joi fields	SQL	V1_245__add_joi_fields.sql	-141752418	tctalent	2025-06-30 16:54:39.927996	10	t
246	1.246	add occupation code joi	SQL	V1_246__add_occupation_code_joi.sql	-1151346582	tctalent	2025-06-30 16:54:39.950665	5	t
247	1.247	add details to users	SQL	V1_247__add_details_to_users.sql	-1953920956	tctalent	2025-06-30 16:54:39.97197	5	t
248	1.248	add country obj to job	SQL	V1_248__add_country_obj_to_job.sql	1264894460	tctalent	2025-06-30 16:54:39.991252	10	t
249	1.249	move joi to table	SQL	V1_249__move_joi_to_table.sql	-1737267264	tctalent	2025-06-30 16:54:40.015753	19	t
250	1.250	add joi field to job	SQL	V1_250__add_joi_field_to_job.sql	-90813315	tctalent	2025-06-30 16:54:40.0586	9	t
251	1.251	add hiring commitment job	SQL	V1_251__add_hiring_commitment_job.sql	-258211837	tctalent	2025-06-30 16:54:40.085195	9	t
252	1.252	add sf fields to job	SQL	V1_252__add_sf_fields_to_job.sql	-1141656419	tctalent	2025-06-30 16:54:40.113679	8	t
253	1.253	add visa pathways to joi	SQL	V1_253__add_visa_pathways_to_joi.sql	-1574163447	tctalent	2025-06-30 16:54:40.133198	4	t
254	1.254	create candidate opportunity table	SQL	V1_254__create_candidate_opportunity_table.sql	2112087957	tctalent	2025-06-30 16:54:40.152268	10	t
255	1.255	add sf opp score	SQL	V1_255__add_sf_opp_score.sql	-287285841	tctalent	2025-06-30 16:54:40.17797	5	t
256	1.256	add won cand opp job opp	SQL	V1_256__add_won_cand_opp_job_opp.sql	-1686836578	tctalent	2025-06-30 16:54:40.195289	7	t
257	1.257	add sf emp description	SQL	V1_257__add_sf_emp_description.sql	-758711730	tctalent	2025-06-30 16:54:40.220623	5	t
258	1.258	add opps filter params to saved search	SQL	V1_258__add_opps_filter_params_to_saved_search.sql	-1999759501	tctalent	2025-06-30 16:54:40.275522	14	t
259	1.259	remove unneeded opportunity update fields	SQL	V1_259__remove_unneeded_opportunity_update_fields.sql	-1957751678	tctalent	2025-06-30 16:54:40.320266	12	t
260	1.260	add missing common opportunity fields to salesforce job opp	SQL	V1_260__add_missing_common_opportunity_fields_to_salesforce_job_opp.sql	1071293756	tctalent	2025-06-30 16:54:40.353286	6	t
261	1.261	add pathway assess to visa check	SQL	V1_261__add_pathway_assess_to_visa_check.sql	85087294	tctalent	2025-06-30 16:54:40.374528	3	t
262	1.262	add pathway assess notes to visa	SQL	V1_262__add_pathway_assess_notes_to_visa.sql	891432960	tctalent	2025-06-30 16:54:40.390265	3	t
263	1.263	remove accepting field from job opp	SQL	V1_263__remove_accepting_field_from_job_opp.sql	-606184732	tctalent	2025-06-30 16:54:40.406037	5	t
264	1.264	create default destination partner	SQL	V1_264__create_default_destination_partner.sql	2078258984	tctalent	2025-06-30 16:54:40.425992	10	t
265	1.265	replace null recruiter partners	SQL	V1_265__replace_null_recruiter_partners.sql	-1854544744	tctalent	2025-06-30 16:54:40.451357	5	t
266	1.266	create update job opp id on visa check	SQL	V1_266__create_update_job_opp_id_on_visa_check.sql	-1631424668	tctalent	2025-06-30 16:54:40.476204	9	t
267	1.267	drop contact email	SQL	V1_267__drop_contact_email.sql	854089535	tctalent	2025-06-30 16:54:40.497668	5	t
268	1.268	undo creation of destnation tbb partner	SQL	V1_268__undo_creation_of_destnation_tbb_partner.sql	-1855942915	tctalent	2025-06-30 16:54:40.514876	6	t
269	1.269	add partner source partner and job creator fields	SQL	V1_269__add_partner_source_partner_and_job_creator_fields.sql	120253682	tctalent	2025-06-30 16:54:40.532649	6	t
270	1.270	clean up jobs	SQL	V1_270__clean_up_jobs.sql	576016718	tctalent	2025-06-30 16:54:40.5496	4	t
271	1.271	clean up legacy partner db stuff	SQL	V1_271__clean_up_legacy_partner_db_stuff.sql	2099157	tctalent	2025-06-30 16:54:40.564483	4	t
272	1.272	remove verified field from candidate occupation	SQL	V1_272__remove_verified_field_from_candidate_occupation.sql	-1742510223	tctalent	2025-06-30 16:54:40.579775	3	t
273	1.273	add ca visa job fields	SQL	V1_273__add_ca_visa_job_fields.sql	-243751309	tctalent	2025-06-30 16:54:40.593675	7	t
274	1.274	add user job creator	SQL	V1_274__add_user_job_creator.sql	-336494186	tctalent	2025-06-30 16:54:40.611798	4	t
275	1.275	add english threshold visa job	SQL	V1_275__add_english_threshold_visa_job.sql	-441610802	tctalent	2025-06-30 16:54:40.627646	7	t
276	1.276	rename role sourcepartneradmin to admin	SQL	V1_276__rename_role_sourcepartneradmin_to_admin.sql	-254958986	tctalent	2025-06-30 16:54:40.649014	5	t
277	1.277	update job country id from country text field	SQL	V1_277__update_job_country_id_from_country_text_field.sql	1212954287	tctalent	2025-06-30 16:54:40.674094	9	t
278	1.278	job chats and posts	SQL	V1_278__job_chats_and_posts.sql	575651617	tctalent	2025-06-30 16:54:40.697196	37	t
279	1.279	add email consent fields	SQL	V1_279__add_email_consent_fields.sql	-1563284441	tctalent	2025-06-30 16:54:40.752875	5	t
280	1.280	add type and source partner to job chat	SQL	V1_280__add_type_and_source_partner_to_job_chat.sql	-1510735455	tctalent	2025-06-30 16:54:40.771105	6	t
281	1.281	add shedlock table	SQL	V1_281__add_shedlock_table.sql	1102364179	tctalent	2025-06-30 16:54:40.789523	5	t
282	1.282	removed old visa fields	SQL	V1_282__removed_old_visa_fields.sql	-614883784	tctalent	2025-06-30 16:54:40.806409	4	t
283	1.283	drop country column from salesforce job opp	SQL	V1_283__drop_country_column_from_salesforce_job_opp.sql	-1140960255	tctalent	2025-06-30 16:54:40.820593	4	t
284	1.284	add offer fields to candidate opp	SQL	V1_284__add_offer_fields_to_candidate_opp.sql	1088884717	tctalent	2025-06-30 16:54:40.839745	7	t
285	1.285	add relocating dependants field	SQL	V1_285__add_relocating_dependants_field.sql	1061881826	tctalent	2025-06-30 16:54:40.863244	6	t
286	1.286	add employer table	SQL	V1_286__add_employer_table.sql	-1340476767	tctalent	2025-06-30 16:54:40.892244	24	t
287	1.287	add visa lang threshold fields	SQL	V1_287__add_visa_lang_threshold_fields.sql	-407397572	tctalent	2025-06-30 16:54:40.929115	6	t
288	1.288	update new lang threshold fields	SQL	V1_288__update_new_lang_threshold_fields.sql	-905110429	tctalent	2025-06-30 16:54:40.947247	4	t
289	1.289	add file interview guidance fields	SQL	V1_289__add_file_interview_guidance_fields.sql	-2048564386	tctalent	2025-06-30 16:54:40.966241	5	t
290	1.290	create job chat user table	SQL	V1_290__create_job_chat_user_table.sql	697290226	tctalent	2025-06-30 16:54:40.986	7	t
291	1.291	add monitoring evaluation consent	SQL	V1_291__add_monitoring_evaluation_consent.sql	-1283321690	tctalent	2025-06-30 16:54:41.004305	4	t
292	1.292	add candidate depepdent candidate id index	SQL	V1_292__add_candidate_depepdent_candidate_id_index.sql	-1548037913	tctalent	2025-06-30 16:54:41.019029	5	t
293	1.293	add job chat candidate id field	SQL	V1_293__add_job_chat_candidate_id_field.sql	1319267474	tctalent	2025-06-30 16:54:41.036651	29	t
294	1.294	add intake complete fields	SQL	V1_294__add_intake_complete_fields.sql	-1780857376	tctalent	2025-06-30 16:54:41.081212	10	t
295	1.295	update intake complete fields	SQL	V1_295__update_intake_complete_fields.sql	-1081115295	tctalent	2025-06-30 16:54:41.113807	9	t
296	1.296	add intake search fields	SQL	V1_296__add_intake_search_fields.sql	-1469433893	tctalent	2025-06-30 16:54:41.143235	6	t
297	1.297	create reaction table	SQL	V1_297__create_reaction_table.sql	-1449191478	tctalent	2025-06-30 16:54:41.160228	8	t
298	1.298	create reaction user join table	SQL	V1_298__create_reaction_user_join_table.sql	1164482927	tctalent	2025-06-30 16:54:41.179805	5	t
299	1.299	add last active stage field	SQL	V1_299__add_last_active_stage_field.sql	-1034329207	tctalent	2025-06-30 16:54:41.195886	3	t
300	1.300	default last active stage	SQL	V1_300__default_last_active_stage.sql	1297201669	tctalent	2025-06-30 16:54:41.209544	5	t
301	1.301	rename lang assessment candidate fields	SQL	V1_301__rename_lang_assessment_candidate_fields.sql	-159706605	tctalent	2025-06-30 16:54:41.226006	4	t
302	1.302	add french assessment columns	SQL	V1_302__add_french_assessment_columns.sql	-1188017652	tctalent	2025-06-30 16:54:41.240687	5	t
303	1.303	change partner citizenship id type	SQL	V1_303__change_partner_citizenship_id_type.sql	441206137	tctalent	2025-06-30 16:54:41.258654	26	t
304	1.304	add index on candidate sflink	SQL	V1_304__add_index_on_candidate_sflink.sql	-1329148101	tctalent	2025-06-30 16:54:41.307175	14	t
305	1.305	create help link table	SQL	V1_305__create_help_link_table.sql	-206955276	tctalent	2025-06-30 16:54:41.34064	12	t
306	1.306	add arrest imprison fields	SQL	V1_306__add_arrest_imprison_fields.sql	2006278351	tctalent	2025-06-30 16:54:41.366106	4	t
307	1.307	add help link next step info	SQL	V1_307__add_help_link_next_step_info.sql	-993076645	tctalent	2025-06-30 16:54:41.38341	8	t
308	1.308	add standard help links	SQL	V1_308__add_standard_help_links.sql	-1298006538	tctalent	2025-06-30 16:54:41.403574	40	t
309	1.309	remove unused field job chat.candidate opp id	SQL	V1_309__remove_unused_field_job_chat.candidate_opp_id.sql	856890640	tctalent	2025-06-30 16:54:41.457417	5	t
310	1.310	add users lower username index	SQL	V1_310__add_users_lower_username_index.sql	1112075066	tctalent	2025-06-30 16:54:41.476507	12	t
311	1.311	add canada australia help links	SQL	V1_311__add_canada_australia_help_links.sql	-1349490195	tctalent	2025-06-30 16:54:41.507721	7	t
312	1.312	perf improvements for name search	SQL	V1_312__perf_improvements_for_name_search.sql	767794645	tctalent	2025-06-30 16:54:41.531127	31	t
313	1.313	add evergreen fields	SQL	V1_313__add_evergreen_fields.sql	-831080168	tctalent	2025-06-30 16:54:41.576483	5	t
314	1.314	add unhcr statuses field to saved search	SQL	V1_314__add_unhcr_statuses_field_to_saved_search.sql	-1045400743	tctalent	2025-06-30 16:54:41.592295	3	t
315	1.315	add mou upload fields	SQL	V1_315__add_mou_upload_fields.sql	1870050073	tctalent	2025-06-30 16:54:41.606439	4	t
316	1.316	create link preview table	SQL	V1_316__create_link_preview_table.sql	1813087189	tctalent	2025-06-30 16:54:41.620906	7	t
317	1.317	move relocating dependants field	SQL	V1_317__move_relocating_dependants_field.sql	1743521760	tctalent	2025-06-30 16:54:41.639982	5	t
318	1.318	add avail date convert avail unsure	SQL	V1_318__add_avail_date_convert_avail_unsure.sql	1304822036	tctalent	2025-06-30 16:54:41.65605	4	t
319	1.319	move destination family fields	SQL	V1_319__move_destination_family_fields.sql	-625737065	tctalent	2025-06-30 16:54:41.671836	9	t
320	1.320	move and create destination family fields	SQL	V1_320__move_and_create_destination_family_fields.sql	-77789945	tctalent	2025-06-30 16:54:41.694838	10	t
321	1.321	drop zombie job opp fields	SQL	V1_321__drop_zombie_job_opp_fields.sql	1735699824	tctalent	2025-06-30 16:54:41.717748	6	t
322	1.322	add listall any fields	SQL	V1_322__add_listall_any_fields.sql	-1051342670	tctalent	2025-06-30 16:54:41.751762	14	t
323	1.323	drop zombie cvjc fields	SQL	V1_323__drop_zombie_cvjc_fields.sql	-1048495900	tctalent	2025-06-30 16:54:41.782503	4	t
324	1.324	create table duolingo coupons	SQL	V1_324__create_table_duolingo_coupons.sql	-660339036	tctalent	2025-06-30 16:54:41.802743	9	t
325	1.325	add potential duplicate to candidate and saved search	SQL	V1_325__add_potential_duplicate_to_candidate_and_saved_search.sql	1817865307	tctalent	2025-06-30 16:54:41.823373	4	t
326	1.326	add partner redirect id to partner	SQL	V1_326__add_partner_redirect_id_to_partner.sql	1128419125	tctalent	2025-06-30 16:54:41.838692	3	t
327	1.327	remove old relocating dependants	SQL	V1_327__remove_old_relocating_dependants.sql	1408273640	tctalent	2025-06-30 16:54:41.85417	3	t
328	1.328	add skip candidate search field	SQL	V1_328__add_skip_candidate_search_field.sql	-1965472633	tctalent	2025-06-30 16:54:41.868934	3	t
329	1.329	update duolingo coupon table	SQL	V1_329__update_duolingo_coupon_table.sql	-1973659914	tctalent	2025-06-30 16:54:41.884472	10	t
330	1.330	add public id column to candidate list and search	SQL	V1_330__add_public_id_column_to_candidate_list_and_search.sql	1677493559	tctalent	2025-06-30 16:54:41.912543	9	t
331	1.331	add relocated address fields	SQL	V1_331__add_relocated_address_fields.sql	128396651	tctalent	2025-06-30 16:54:41.940376	12	t
332	1.332	add english assessment score det column	SQL	V1_332__add_english_assessment_score_det_column.sql	-61549788	tctalent	2025-06-30 16:54:41.967356	5	t
333	1.333	add duolingo english test task	SQL	V1_333__add_duolingo_english_test_task.sql	1331679475	tctalent	2025-06-30 16:54:41.991933	7	t
334	1.334	rename tbbShortName to tcShortName	SQL	V1_334__rename_tbbShortName_to_tcShortName.sql	-473673515	tctalent	2025-06-30 16:54:42.012359	5	t
335	1.335	add email verification	SQL	V1_335__add_email_verification.sql	-360616603	tctalent	2025-06-30 16:54:42.028004	4	t
336	1.336	rename task helplink to doclink	SQL	V1_336__rename_task_helplink_to_doclink.sql	2011033157	tctalent	2025-06-30 16:54:42.042557	3	t
337	1.337	add candidate muted and all notifications	SQL	V1_337__add_candidate_muted_and_all_notifications.sql	1936506143	tctalent	2025-06-30 16:54:42.057123	5	t
338	1.338	create duolingo extra fields table	SQL	V1_338__create_duolingo_extra_fields_table.sql	-115115733	tctalent	2025-06-30 16:54:42.073369	8	t
339	1.339	add claim duolingo coupon button task 	SQL	V1_339__add_claim_duolingo_coupon_button_task_.sql	520709921	tctalent	2025-06-30 16:54:42.092391	4	t
340	1.340	create offer to assist table	SQL	V1_340__create_offer_to_assist_table.sql	1218938675	tctalent	2025-06-30 16:54:42.109171	41	t
341	1.341	add partner hashKey and authorizations	SQL	V1_341__add_partner_hashKey_and_authorizations.sql	-278408900	tctalent	2025-06-30 16:54:42.166204	6	t
342	1.342	add candidate registered by and partner public id	SQL	V1_342__add_candidate_registered_by_and_partner_public_id.sql	2000160291	tctalent	2025-06-30 16:54:42.195153	13	t
343	1.343	remove migration nationality field	SQL	V1_343__remove_migration_nationality_field.sql	-1624547533	tctalent	2025-06-30 16:54:42.221608	3	t
344	1.344	update and remove migration language field	SQL	V1_344__update_and_remove_migration_language_field.sql	-2077001141	tctalent	2025-06-30 16:54:42.235332	53	t
345	1.345	delete extra duolingo task	SQL	V1_345__delete_extra_duolingo_task.sql	-801758969	tctalent	2025-06-30 16:54:42.305291	9	t
346	1.346	add duolingo test type	SQL	V1_346__add_duolingo_test_type.sql	-18730177	tctalent	2025-06-30 16:54:42.330545	6	t
347	1.347	change duolingo task desplay name	SQL	V1_347__change_duolingo_task_desplay_name.sql	494609627	tctalent	2025-06-30 16:54:42.360756	9	t
348	1.348	add new languages and update mapping	SQL	V1_348__add_new_languages_and_update_mapping.sql	-1294952651	tctalent	2025-06-30 16:54:42.380567	28	t
349	1.349	fix candidate skills  table seq generation	SQL	V1_349__fix_candidate_skills _table_seq_generation.sql	1780237666	tctalent	2025-06-30 16:54:42.423738	3	t
350	1.350	add indexes for standard ids	SQL	V1_350__add_indexes_for_standard_ids.sql	1434513918	tctalent	2025-06-30 16:54:42.43836	11	t
351	1.351	add change password field to candidate	SQL	V1_351__add_change_password_field_to_candidate.sql	-1810578680	tctalent	2025-06-30 16:54:42.461487	5	t
352	1.352	remove migration education major field	SQL	V1_352__remove_migration_education_major_field.sql	-586040301	tctalent	2025-06-30 16:54:42.478068	6	t
353	1.353	update country names	SQL	V1_353__update_country_names.sql	-1535522503	tctalent	2025-06-30 16:54:42.495731	5	t
354	1.354	update survey type	SQL	V1_354__update_survey_type.sql	-435490396	tctalent	2025-06-30 16:54:42.512609	18	t
355	1.355	add whatsapp index	SQL	V1_355__add_whatsapp_index.sql	1342221428	tctalent	2025-06-30 16:54:42.548302	8	t
\.


--
-- Data for Name: help_link; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.help_link (id, country_id, case_stage, job_stage, label, link, focus, next_step_name, next_step_text, next_step_days, created_by, created_date, updated_by, updated_date) FROM stdin;
1	\N	\N	prospect	Prospect (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#prospect	\N	\N	\N	\N	\N	\N	\N	\N
2	\N	\N	briefing	Briefing (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#briefing	\N	\N	\N	\N	\N	\N	\N	\N
3	\N	\N	pitching	Pitching (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#pitching	\N	\N	\N	\N	\N	\N	\N	\N
4	\N	\N	identifyingRoles	Identifying roles (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#identifying-roles	\N	\N	\N	\N	\N	\N	\N	\N
5	\N	\N	candidateSearch	Candidate search (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#candidate-search	\N	\N	\N	\N	\N	\N	\N	\N
6	\N	\N	visaEligibility	Visa eligibility (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#visa-eligibility	\N	\N	\N	\N	\N	\N	\N	\N
7	\N	\N	cvPreparation	CV preparation (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#cv-preparation	\N	\N	\N	\N	\N	\N	\N	\N
8	\N	\N	cvReview	CV review (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#cv-review	\N	\N	\N	\N	\N	\N	\N	\N
9	\N	\N	recruitmentProcess	Recruitment process (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#recruitment-process	\N	\N	\N	\N	\N	\N	\N	\N
10	\N	\N	jobOffer	Job offer (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#job-offer	\N	\N	\N	\N	\N	\N	\N	\N
11	\N	\N	visaPreparation	Visa preparation (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#visa-preparation	\N	\N	\N	\N	\N	\N	\N	\N
12	\N	\N	postHireEngagement	Post hire engagement (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#post-hire-engagement	\N	\N	\N	\N	\N	\N	\N	\N
13	\N	\N	hiringCompleted	Hiring completed (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#hiring-completed	\N	\N	\N	\N	\N	\N	\N	\N
14	\N	\N	ineligibleEmployer	Ineligible employer (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#ineligible-employer	\N	\N	\N	\N	\N	\N	\N	\N
15	\N	\N	ineligibleOccupation	Ineligible occupation (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#ineligible-occupation	\N	\N	\N	\N	\N	\N	\N	\N
16	\N	\N	ineligibleRegion	Ineligible region (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#ineligible-region	\N	\N	\N	\N	\N	\N	\N	\N
17	\N	\N	noInterest	No interest (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#no-interest	\N	\N	\N	\N	\N	\N	\N	\N
18	\N	\N	noJobOffer	No job offer (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#no-job-offer	\N	\N	\N	\N	\N	\N	\N	\N
19	\N	\N	noPrPathway	No PR pathway (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#no-pr-pathway	\N	\N	\N	\N	\N	\N	\N	\N
20	\N	\N	noSuitableCandidates	No suitable candidates (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#no-suitable-candidates	\N	\N	\N	\N	\N	\N	\N	\N
21	\N	\N	noVisa	No visa (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#no-visa	\N	\N	\N	\N	\N	\N	\N	\N
22	\N	\N	tooExpensive	Too expensive (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#too-expensive	\N	\N	\N	\N	\N	\N	\N	\N
23	\N	\N	tooHighWage	Too high wage (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#too-high-wage	\N	\N	\N	\N	\N	\N	\N	\N
24	\N	\N	tooLong	Too long (Job)	https://tchelp.tettra.site/pages/job-opportunity-stages#too-long	\N	\N	\N	\N	\N	\N	\N	\N
25	\N	prospect	\N	Prospect (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#prospect	\N	\N	\N	\N	\N	\N	\N	\N
26	\N	miniIntake	\N	Mini intake (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#mini-intake	\N	\N	\N	\N	\N	\N	\N	\N
27	\N	fullIntake	\N	Full intake (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#full-intake	\N	\N	\N	\N	\N	\N	\N	\N
28	\N	visaEligibility	\N	Visa eligibility (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#visa-eligibility	\N	\N	\N	\N	\N	\N	\N	\N
29	\N	cvPreparation	\N	CV preparation (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#cv-preparation	\N	\N	\N	\N	\N	\N	\N	\N
30	\N	cvReview	\N	CV review (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#cv-review	\N	\N	\N	\N	\N	\N	\N	\N
31	\N	oneWayPreparation	\N	1 way preparation (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#1-way-preparation	\N	\N	\N	\N	\N	\N	\N	\N
32	\N	oneWayReview	\N	1 way review (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#1-way-review	\N	\N	\N	\N	\N	\N	\N	\N
33	\N	testPreparation	\N	Test preparation (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#test-preparation	\N	\N	\N	\N	\N	\N	\N	\N
34	\N	testing	\N	Testing (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#testing	\N	\N	\N	\N	\N	\N	\N	\N
35	\N	twoWayPreparation	\N	2 way preparation (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#2-way-preparation	\N	\N	\N	\N	\N	\N	\N	\N
36	\N	twoWayReview	\N	2 way review (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#2-way-review	\N	\N	\N	\N	\N	\N	\N	\N
37	\N	offer	\N	Offer (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#offer	\N	\N	\N	\N	\N	\N	\N	\N
38	\N	acceptance	\N	Acceptance (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#acceptance	\N	\N	\N	\N	\N	\N	\N	\N
39	\N	provincialVisaPreparation	\N	Provincial visa preparation (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#provincial-visa-preparation	\N	\N	\N	\N	\N	\N	\N	\N
40	\N	provincialVisaProcessing	\N	Provincial visa processing (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#provincial-visa-processing	\N	\N	\N	\N	\N	\N	\N	\N
41	\N	visaPreparation	\N	Visa preparation (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#visa-preparation	\N	\N	\N	\N	\N	\N	\N	\N
42	\N	visaProcessing	\N	Visa processing (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#visa-processing	\N	\N	\N	\N	\N	\N	\N	\N
43	\N	relocating	\N	Relocating (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#relocating	\N	\N	\N	\N	\N	\N	\N	\N
44	\N	relocated	\N	Relocated (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#relocated	\N	\N	\N	\N	\N	\N	\N	\N
45	\N	settled	\N	Settled (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#settled	\N	\N	\N	\N	\N	\N	\N	\N
46	\N	durableSolution	\N	Durable solution (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#durable-solution	\N	\N	\N	\N	\N	\N	\N	\N
47	\N	noJobOffer	\N	No job offer (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#no-job-offer	\N	\N	\N	\N	\N	\N	\N	\N
48	\N	noVisa	\N	No visa (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#no-visa	\N	\N	\N	\N	\N	\N	\N	\N
49	\N	notFitForRole	\N	Not fit for role (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#not-fit-for-role	\N	\N	\N	\N	\N	\N	\N	\N
50	\N	notEligibleForTC	\N	Not eligible for TC (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#not-eligible-for-tc	\N	\N	\N	\N	\N	\N	\N	\N
51	\N	notEligibleForVisa	\N	Not eligible for visa (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#not-eligible-for-visa	\N	\N	\N	\N	\N	\N	\N	\N
52	\N	noInterview	\N	No interview (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#no-interview	\N	\N	\N	\N	\N	\N	\N	\N
53	\N	candidateLeavesDestination	\N	Candidate leaves destination (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#candidate-leaves-destination	\N	\N	\N	\N	\N	\N	\N	\N
54	\N	candidateRejectsOffer	\N	Candidate rejects offer (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#candidate-rejects-offer	\N	\N	\N	\N	\N	\N	\N	\N
55	\N	candidateUnreachable	\N	Candidate unreachable (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#candidate-unreachable	\N	\N	\N	\N	\N	\N	\N	\N
56	\N	candidateWithdraws	\N	Candidate withdraws (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#candidate-withdraws	\N	\N	\N	\N	\N	\N	\N	\N
57	\N	jobOfferRetracted	\N	Job offer retracted (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#job-offer-retracted	\N	\N	\N	\N	\N	\N	\N	\N
58	\N	relocatedNoJobOfferPathway	\N	Relocated no job offer pathway (Case)	https://tchelp.tettra.site/pages/candidate-opportunity-stages#relocated-no-job-offer-pathway	\N	\N	\N	\N	\N	\N	\N	\N
\.


--
-- Data for Name: industry; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.industry (id, name, status) FROM stdin;
0	Unknown	inactive
\.


--
-- Data for Name: job_chat; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.job_chat (id, created_by, created_date, job_id, updated_by, updated_date, type, source_partner_id, candidate_id) FROM stdin;
\.


--
-- Data for Name: job_chat_user; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.job_chat_user (job_chat_id, user_id, last_read_post_id) FROM stdin;
\.


--
-- Data for Name: job_opp_intake; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.job_opp_intake (id, recruitment_process, employer_cost_commitment, location, location_details, salary_range, benefits, language_requirements, employment_experience, education_requirements, skill_requirements, occupation_code, min_salary, visa_pathways) FROM stdin;
\.


--
-- Data for Name: job_suggested_saved_search; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.job_suggested_saved_search (tc_job_id, saved_search_id) FROM stdin;
\.


--
-- Data for Name: language; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.language (id, name, status, iso_code) FROM stdin;
0	Unknown	inactive	\N
10000	Acholi	active	\N
10001	Afrikaans	active	\N
10002	Akan	active	\N
10003	Albanian	active	\N
10004	Amharic	active	\N
10005	Ashante	active	\N
10006	Asl	active	\N
10007	Assyrian	active	\N
10008	Azerbaijani	active	\N
10009	Azeri	active	\N
10010	Bajuni	active	\N
10011	Basque	active	\N
10012	Behdini	active	\N
10013	Belorussian	active	\N
10014	Bengali	active	\N
10015	Berber	active	\N
10016	Bosnian	active	\N
10017	Bravanese	active	\N
10018	Bulgarian	active	\N
10019	Burmese	active	\N
10020	Cakchiquel	active	\N
10021	Cambodian	active	\N
10022	Cantonese	active	\N
10023	Catalan	active	\N
10024	Chaldean	active	\N
10025	Chamorro	active	\N
10026	Chao-chow	active	\N
10027	Chavacano	active	\N
10028	Chin	active	\N
10029	Chuukese	active	\N
10030	Cree	active	\N
10031	Croatian	active	\N
10032	Czech	active	\N
10033	Dakota	active	\N
10034	Danish	active	\N
10036	Dinka	active	\N
10037	Diula	active	\N
10038	Dutch	active	\N
10039	Edo	active	\N
10040	Estonian	active	\N
10041	Ewe	active	\N
10042	Fante	active	\N
10043	Fijian Hindi	active	\N
10044	Finnish	active	\N
10045	Flemish	active	\N
10046	French Canadian	active	\N
10047	Fukienese	active	\N
10048	Fula	active	\N
10049	Fulani	active	\N
10050	Fuzhou	active	\N
10051	Ga	active	\N
10052	Gaddang	active	\N
10053	Gaelic	active	\N
10054	Gaelic-irish	active	\N
10055	Gaelic-scottish	active	\N
10056	Georgian	active	\N
10057	Gorani	active	\N
10058	Gujarati	active	\N
10059	Haitian Creole	active	\N
10060	Hakka	active	\N
10061	Hakka-chinese	active	\N
10062	Hausa	active	\N
10063	Hebrew	active	\N
10064	Hindi	active	\N
10065	Hmong	active	\N
10066	Hungarian	active	\N
10067	Ibanag	active	\N
10068	Ibo	active	\N
10069	Icelandic	active	\N
10070	Igbo	active	\N
10071	Ilocano	active	\N
10072	Indonesian	active	\N
10073	Inuktitut	active	\N
10074	Jakartanese	active	\N
10075	Japanese	active	\N
10076	Javanese	active	\N
10077	Kanjobal	active	\N
10078	Karen	active	\N
10079	Karenni	active	\N
10080	Kashmiri	active	\N
10081	Kazakh	active	\N
10082	Kikuyu	active	\N
10083	Kinyarwanda	active	\N
10084	Kirundi	active	\N
10085	Korean	active	\N
10086	Kosovan	active	\N
10087	Kotokoli	active	\N
10088	Krio	active	\N
10089	Kurdish	active	\N
10090	Kurmanji	active	\N
10091	Kyrgyz	active	\N
10092	Lakota	active	\N
10093	Laotian	active	\N
10094	Latvian	active	\N
10095	Lingala	active	\N
10096	Lithuanian	active	\N
10097	Luganda	active	\N
10098	Luo	active	\N
10099	Maay	active	\N
10100	Macedonian	active	\N
10101	Malay	active	\N
10102	Malayalam	active	\N
10103	Maltese	active	\N
10104	Mandarin	active	\N
10105	Mandingo	active	\N
10106	Mandinka	active	\N
10107	Marathi	active	\N
10108	Marshallese	active	\N
10109	Mien	active	\N
10110	Mina	active	\N
10111	Mirpuri	active	\N
10112	Mixteco	active	\N
10113	Moldavan	active	\N
10114	Mongolian	active	\N
10115	Montenegrin	active	\N
10116	Navajo	active	\N
10117	Neapolitan	active	\N
10118	Nepali	active	\N
10119	Nigerian Pidgin	active	\N
10120	Norwegian	active	\N
10121	Oromo	active	\N
10122	Pahari	active	\N
10123	Papago	active	\N
10124	Papiamento	active	\N
10125	Patois	active	\N
10126	Pidgin English	active	\N
10127	Polish	active	\N
10128	Portug.creole	active	\N
10129	Pothwari	active	\N
10130	Pulaar	active	\N
10131	Punjabi	active	\N
10132	Putian	active	\N
10133	Quichua	active	\N
10134	Romanian	active	\N
10135	Samoan	active	\N
10136	Serbian	active	\N
10137	Shanghainese	active	\N
10138	Shona	active	\N
10139	Sichuan	active	\N
10140	Sicilian	active	\N
10141	Sinhalese	active	\N
10142	Slovak	active	\N
10143	Sorani	active	\N
10144	Sudanese Arabic	active	\N
10145	Susu	active	\N
10146	Swahili	active	\N
10147	Swedish	active	\N
10148	Sylhetti	active	\N
10149	Tagalog	active	\N
10150	Taiwanese	active	\N
10151	Tajik	active	\N
10152	Tamil	active	\N
10153	Telugu	active	\N
10154	Thai	active	\N
10155	Tibetan	active	\N
10156	Tigre	active	\N
10157	Tigrinya	active	\N
10158	Toishanese	active	\N
10159	Tongan	active	\N
10160	Toucouleur	active	\N
10161	Trique	active	\N
10162	Tshiluba	active	\N
10163	Twi	active	\N
10164	Ukrainian	active	\N
10165	Urdu	active	\N
10166	Uyghur	active	\N
10167	Uzbek	active	\N
10168	Vietnamese	active	\N
10169	Visayan	active	\N
10170	Welsh	active	\N
10171	Wolof	active	\N
10172	Yiddish	active	\N
10173	Yoruba	active	\N
10174	Yupik	active	\N
10035	Dari	active	fa
10175	Balochi (Southern Balochi)	active	bcc
10176	Balochi (Western Balochi)	active	bgn
10177	Balochi (Eastern Balochi)	active	bgp
10178	brahui	active	brh
10179	circassian (adyghe)	active	ady
10180	circassian (kabardian)	active	kbd
10181	fur	active	fvr
10182	latin	active	la
10183	nauruan	active	nau
10184	rohingya	active	rhg
10185	sindhi	active	snd
10186	syriac	active	syr
10187	zaghawa	active	zag
10188	International Sign	active	ils
\.


--
-- Data for Name: language_level; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.language_level (id, name, status, level) FROM stdin;
0	Unknown	inactive	0
353	No Proficiency	active	5
\.


--
-- Data for Name: link_preview; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.link_preview (id, chat_post_id, url, title, description, image_url, domain, favicon_url) FROM stdin;
\.


--
-- Data for Name: nationality; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.nationality (id, name, status) FROM stdin;
0	Unknown	inactive
\.


--
-- Data for Name: occupation; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.occupation (id, name, status, isco08_code) FROM stdin;
0	Unknown	inactive	\N
10002	Stonemason	active	7113
10003	Fishmonger	active	7511
10004	Cartographer	active	2165
10005	Botanist	active	2131
10006	Pathologist	active	2212
10007	Anthropologist	active	2632
10008	Optician, ophthalmic	active	2267
10009	Tuner, musical instrument	active	7312
10010	Geophysicist	active	2114
10011	Engineer, diesel	active	2144
10012	Mechanic, diesel: motor vehicle	active	7231
10013	Mechanic, engine: diesel (except motor vehicle)	active	7233
10014	Scientist, data mining	active	2529
10015	Other Teaching Professionals	active	235
10016	Other Health Associate Professionals	active	325
10017	Other Personal Services Workers	active	516
10018	Other Sales Workers	active	524
10019	Other Stationary Plant and Machine Operators	active	818
10020	Zoologist	active	2131
10000	Appraiser	active	2632
10001	Dental Assistants and Therapists	active	3434
10021	Inspector, fire	active	3112
\.


--
-- Data for Name: offer_to_assist; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.offer_to_assist (id, created_by, created_date, updated_by, updated_date, additional_notes, partner_id, public_id, reason) FROM stdin;
\.


--
-- Data for Name: partner; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.partner (default_source_partner, id, logo, name, status, registration_landing_page, registration_domain, website_url, abbreviation, notification_email, default_partner_ref, sflink, auto_assignable, default_contact_id, default_job_creator, source_partner, job_creator, employer_id, redirect_partner_id, public_api_key_hash, public_api_authorities, public_id) FROM stdin;
f	2	assets/images/unhcrLogo.png	UNHCR	active	\N	unhcrtalent.org	https://www.unhcr.org/	\N	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
f	3	assets/images/hiasLogo.png	HIAS	active	\N	hiastalent.org	https://www.hias.org/	\N	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
f	4	assets/images/iomLogo.png	IOM	active	\N	iomtalent.org	https://www.iom.int/	\N	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
f	5	assets/images/refugePointLogo.png	Refuge Point	active	\N	refugepointtalent.org	https://www.refugepoint.org/	\N	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
f	6	assets/images/dignityLogo.png	Dignity for Children	active	\N	dignity.displacedtalent.org	https://dignityforchildren.org/	Dignity	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
f	7	assets/images/crsLogo.png	Catholic Relief Services	active	\N	crs.displacedtalent.org	https://www.crs.org/	CRS	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
f	8	assets/images/afghanTalentLogo.png	Afghan Talent	active	https://www.afghantalent.us/	afghantalent.displacedtalent.org	https://www.afghantalent.us/	AT	\N	f	\N	f	\N	f	t	f	\N	\N	\N	\N	\N
t	1	assets/images/tbbLogo.png	Talent Beyond Boundaries	active	https://www.talentbeyondboundaries.org/talentcatalog/	tbbtalent.org	https://talentbeyondboundaries.org	\N	\N	t	\N	f	\N	t	t	t	\N	\N	\N	\N	\N
\.


--
-- Data for Name: partner_job; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.partner_job (partner_id, tc_job_id, contact_id) FROM stdin;
\.


--
-- Data for Name: partner_source_country; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.partner_source_country (partner_id, country_id) FROM stdin;
\.


--
-- Data for Name: reaction; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.reaction (id, chat_post_id, emoji) FROM stdin;
\.


--
-- Data for Name: reaction_user; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.reaction_user (reaction_id, user_id) FROM stdin;
\.


--
-- Data for Name: root_request; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.root_request (id, ip_address, partner_abbreviation, query_string, request_url, "timestamp", utm_campaign, utm_content, utm_medium, utm_source, utm_term, referrer_param) FROM stdin;
\.


--
-- Data for Name: salesforce_job_opp; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.salesforce_job_opp (sf_id, closed, employer, name, stage, stage_order, account_id, owner_id, id, submission_due_date, submission_list_id, contact_user_id, job_summary, recruiter_partner_id, suggested_list_id, created_by, created_date, published_by, published_date, updated_by, updated_date, exclusion_list_id, description, salary_range, location, location_details, benefits, language_requirements, employment_experience, education_requirements, skill_requirements, occupation_code, country_object_id, job_opp_intake_id, hiring_commitment, employer_website, employer_hired_internationally, opportunity_score, won, employer_description, closing_comments, next_step, next_step_due_date, employer_id, evergreen, evergreen_child_id, skip_candidate_search) FROM stdin;
\.


--
-- Data for Name: saved_list; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.saved_list (id, status, name, fixed, watcher_ids, created_by, created_date, updated_by, updated_date, saved_search_id, global, saved_search_source_id, displayed_fields_long, displayed_fields_short, folderlink, foldercvlink, folderjdlink, registered_job, description, published_doc_link, tc_short_name, sf_opp_is_closed, job_id, file_jd_name, file_jd_link, file_joi_name, file_joi_link, file_interview_guidance_name, file_interview_guidance_link, file_mou_name, file_mou_link, public_id) FROM stdin;
1	active	TestCandidates	t	\N	1	2025-06-30 12:24:53.778975+00	1	2025-06-30 12:24:53.778975+00	\N	t	\N	\N	\N	\N	\N	\N	f	\N	\N	\N	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	ZV3S20aoSfibZaXMDv_Dew
\.


--
-- Data for Name: saved_search; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.saved_search (id, status, name, keyword, statuses, gender, occupation_ids, or_profile_keyword, verified_occupation_ids, verified_occupation_search_type, nationality_ids, nationality_search_type, country_ids, other_language_id, un_registered, last_modified_from, last_modified_to, created_from, created_to, min_age, max_age, education_major_ids, created_by, created_date, updated_by, updated_date, min_education_level, english_min_spoken_level, english_min_written_level, other_min_spoken_level, other_min_written_level, type, fixed, reviewable, watcher_ids, global, default_search, simple_query_string, min_yrs, max_yrs, displayed_fields_long, displayed_fields_short, default_save_selection_list_id, description, survey_type_ids, exclusion_list_id, partner_ids, job_id, country_search_type, rego_referrer_param, any_opps, closed_opps, relocated_opps, mini_intake_completed, full_intake_completed, unhcr_statuses, list_all_ids, list_all_search_type, list_any_ids, list_any_search_type, potential_duplicate, public_id) FROM stdin;
\.


--
-- Data for Name: search_join; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.search_join (id, search_id, child_search_id, search_type) FROM stdin;
\.


--
-- Data for Name: shedlock; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.shedlock (name, lock_until, locked_at, locked_by) FROM stdin;
\.


--
-- Data for Name: survey_type; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.survey_type (id, name, status) FROM stdin;
1	Information Session	active
2	Community centre posting - flyers	active
4	Facebook	active
6	Outreach worker	active
7	NGO	active
8	Other	active
9	UNHCR	active
10	US-Afghan	inactive
11	Al Ghurair Foundation	active
12	ULYP	active
13	Techfugees	active
3	From a friend	inactive
14	Friend or colleague referral	active
5	Facebook - through an organisation	inactive
15	Online Google search	active
16	Instagram	active
17	LinkedIn	active
18	X	active
19	WhatsApp	active
20	YouTube	active
21	University or school referral	active
22	Employer referral	active
23	Event or webinar	active
\.


--
-- Data for Name: system_language; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.system_language (id, language, label, status, created_by, created_date, updated_by, updated_date) FROM stdin;
1	en	English	active	1	2025-06-30 12:24:33.884944+00	\N	\N
2	ar	عربى	active	1	2025-06-30 12:24:33.884944+00	\N	\N
3	fa	Dari	active	1	2025-06-30 12:24:37.920794+00	\N	\N
\.


--
-- Data for Name: task; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.task (id, admin, created_by, created_date, days_to_complete, description, doc_link, name, optional, task_type, updated_by, updated_date, upload_subfolder_name, upload_type, uploadable_file_types, candidate_answer_field, display_name, explicit_allowed_answers) FROM stdin;
1	f	1	2025-06-30 12:24:38.164772+00	7	Bachelors degree certificate	\N	degree	f	UploadTask	\N	\N	Qualification	degree	\N	\N	Degree certificate	\N
2	f	1	2025-06-30 12:24:38.164772+00	7	Transcript with grades for all years	\N	degreeTranscript	f	UploadTask	\N	\N	Qualification	degreeTranscript	\N	\N	Degree transcript	\N
3	f	1	2025-06-30 12:24:38.164772+00	7	Translation of degree transcript	\N	degreeTranscriptTrans	f	UploadTask	\N	\N	Qualification	degreeTranscriptTrans	\N	\N	Degree transcript translation	\N
4	f	1	2025-06-30 12:24:38.164772+00	7	Letter from your university stating, in English, that you studied in English	\N	studiedInEnglish	f	UploadTask	\N	\N	Qualification	studiedInEnglish	\N	\N	Letter studied in English	\N
5	f	1	2025-06-30 12:24:38.164772+00	14	Must have 6 months validity after arrival to the UK. Birth date must be present and correct	\N	passport	f	UploadTask	\N	\N	Identity	passport	\N	\N	Passport	\N
6	f	1	2025-06-30 12:24:38.194151+00	14	\N	\N	otherId	f	UploadTask	\N	\N	Identity	otherId	\N	\N	Residency Card or Drivers License	\N
7	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	otherIdTrans	f	UploadTask	\N	\N	Identity	otherIdTrans	\N	\N	Residency Card or Drivers License Translation	\N
8	f	1	2025-06-30 12:24:38.194151+00	14	You can use an attestation from your local Mayor if you have no other proof of address	\N	proofAddress	f	UploadTask	\N	\N	Address	proofAddress	\N	\N	Proof of address	\N
9	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	proofAddressTrans	f	UploadTask	\N	\N	Address	proofAddressTrans	\N	\N	Proof of address translation	\N
10	f	1	2025-06-30 12:24:38.194151+00	14	You can use an attestation from your local Mayor if you have no other proof of address	\N	residenceAttest	f	UploadTask	\N	\N	Address	residenceAttest	\N	\N	Residence Attestation	\N
11	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	residenceAttestTrans	f	UploadTask	\N	\N	Address	residenceAttestTrans	\N	\N	Residence Attestation	\N
12	f	1	2025-06-30 12:24:38.194151+00	14	From every country you lived in for over 6 months in the last 5 years.	\N	policeCheck	f	UploadTask	\N	\N	Character	policeCheck	\N	\N	Police Check	\N
13	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	policeCheckTrans	f	UploadTask	\N	\N	Character	policeCheckTrans	\N	\N	Police Check Translation	\N
14	f	1	2025-06-30 12:24:38.194151+00	14	Optional - not needed if you are a recent graduate or do not have registration	\N	licencing	t	UploadTask	\N	\N	Registration	licencing	\N	\N	Licensing Registration	\N
15	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	licencingTrans	t	UploadTask	\N	\N	Registration	licencingTrans	\N	\N	Licensing Registration Translation	\N
16	f	1	2025-06-30 12:24:38.194151+00	14	eg from Ministry of Public Health in Lebanon	\N	conductMinistry	f	UploadTask	\N	\N	Registration	conductMinistry	\N	\N	Certificate of Good Conduct from Ministry of Public Health	\N
17	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	conductMinistryTrans	f	UploadTask	\N	\N	Registration	conductMinistryTrans	\N	\N	Certificate of Good Conduct from Ministry of Public Health Translation	\N
18	f	1	2025-06-30 12:24:38.194151+00	14	From most recent healthcare employer or education if recently graduated	\N	conductEmployer	f	UploadTask	\N	\N	Registration	conductEmployer	\N	\N	Certificate of Good Conduct from Employer/Education	\N
19	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	conductEmployerTrans	f	UploadTask	\N	\N	Registration	conductEmployerTrans	\N	\N	Certificate of Good Conduct from Employer/Education Translation	\N
22	f	1	2025-06-30 12:24:38.194151+00	14	From employer	\N	offer	f	UploadTask	\N	\N	Employer	offer	\N	\N	Letter of offer	\N
24	f	1	2025-06-30 12:24:38.194151+00	14	Candidates should also send directly to trust	\N	cos	f	UploadTask	\N	\N	Immigration	cos	\N	\N	Certificate of sponsorship (COS)	\N
25	f	1	2025-06-30 12:24:38.194151+00	14	Candidates just need to provide email address or phone number for referees, not document. Need to cover a full three years from a professional institution i.e work or education. If there is a gap of over 3 months, you need to provide character referees who are working within a professional environment.	\N	references	f	UploadTask	\N	\N	Immigration	references	\N	\N	References	\N
26	f	1	2025-06-30 12:24:38.302528+00	7	Please sign and upload collaboration agreement. You can download the document by clicking on the View Task Help button.	https://www.talentbeyondboundaries.org/talentcatalog/tasks/upload-collaboration-agreement	collaborationAgreement	f	UploadTask	\N	\N	Engagement	collaborationAgreement	\N	\N	Collaboration Agreement	\N
27	f	1	2025-06-30 12:24:38.302528+00	7	Please fill out, sign and upload the Talent Beyond Boundaries information release form. You can download the document by clicking on the View Task Help button.	https://www.talentbeyondboundaries.org/talentcatalog/tasks/upload-tbb-info-release-form	infoReleaseForm	f	UploadTask	\N	\N	Engagement	infoReleaseForm	\N	\N	Information Release Form	\N
23	f	1	2025-06-30 12:24:38.194151+00	14	Optional - upload result if you have taken an exam	\N	englishExam	t	UploadTask	\N	\N	Language	englishExam	\N	\N	English exam results - OET/IELTS	\N
20	f	1	2025-06-30 12:24:38.194151+00	14	Covid 19 vaccination certificate	\N	vaccination	f	UploadTask	\N	\N	Medical	vaccination	\N	\N	Vaccination records	\N
21	f	1	2025-06-30 12:24:38.194151+00	14	Translation of original document	\N	vaccinationTrans	f	UploadTask	\N	\N	Medical	vaccinationTrans	\N	\N	Vaccination records translation	\N
28	f	1	2025-06-30 12:24:38.428776+00	7	\N	\N	idCard	f	UploadTask	\N	\N	Identity	idCard	\N	\N	ID Card	\N
29	f	1	2025-06-30 12:24:38.428776+00	7	Copy of your UNHCR or UNRWA Registration Certificate or Barcode (if applicable). If you are not registered with the UNHCR or UNRWA, please click "abandon task" and add a comment explaining the reason why you are not registered.	\N	unhcrUnrwaRegCard	f	UploadTask	\N	\N	Registration	unhcrUnrwaRegCard	\N	\N	UNHCR/UNRWA Registration Card	\N
31	f	1	2025-06-30 12:24:38.635752+00	7	Please read the collaboration agreement and check the box to agree.	https://static1.squarespace.com/static/5dc0262432cd095744bf1bf2/t/6213172072e2bb66b18cd8b5/1645418273053/Candidate_TBB+Collaboration+Agreement+-+UPDATED+2021.pdf	collaborationAgreementSimple	f	Task	\N	\N	\N	\N	\N	\N	Collaboration Agreement	\N
32	f	1	2025-06-30 12:24:38.635752+00	7	Please select which option best describes your status.	\N	unhcrStatus	f	QuestionTask	\N	\N	\N	\N	\N	unhcrStatus	What is your status with UNHCR?	\N
33	f	1	2025-06-30 12:24:38.635752+00	7	If you have any restrictions please describe in detail so we can do our best to try and accommodate.	\N	livingRestrictions	f	QuestionTask	\N	\N	\N	\N	\N	\N	Do you have any restrictions when it comes to living with other people?	\N
30	f	1	2025-06-30 12:24:38.635752+00	7	Please upload a photo of the visa to let us know you have received it. If you have not yet received your visa, do not worry it will come. When it does arrive please return to task and upload a photo of the visa.	\N	visaReturned	f	UploadTask	\N	\N	Immigration	visa	\N	\N	Have you received your passport back with your visa in it?	\N
34	f	1	2025-06-30 12:24:38.667933+00	7	The IOM have asked if candidates can do some pre-departure filming (on your mobile phones) that could then be collected and put in a film they are putting together about your arrivals. If you have some videos you would like to put forward, please upload them to a shareable drive (Dropbox, Google Drive etc) and provide the shareable link below.	\N	predepartureVideo	f	QuestionTask	\N	\N	\N	\N	\N	\N	Provide link to your pre-departure video/s	\N
35	f	1	2025-06-30 12:24:38.682491+00	7	Please fill out the Expression of Interest form for the Australian PhysioTherapy Council, the form can be downloaded by clicking on the Task Help button. Once completed, please upload the filled out form below.	https://drive.google.com/file/d/1gIhD8vnJtHN-LmYtRmaZ7n4U_3-x-FPl/view?usp=sharing	apcInterestForm	f	UploadTask	\N	\N	Experience	apcInterestForm	\N	\N	Australian Physiotherapy Council - Expression of Interest	\N
36	f	1	2025-06-30 12:24:38.701803+00	7	Please upload a screenshot of the results screen once completed your test. It should show your Reading, Listening, Language Knowledge and Overall scores.	\N	OETPulseResults	f	UploadTask	\N	\N	Language	OETPulseResults	\N	\N	OET Pulse Test result (Please upload a screenshot of your result)	\N
37	f	1	2025-06-30 12:24:38.701803+00	7	Please write your score below (numeric value)	\N	oetOverallScore	f	QuestionTask	\N	\N	\N	\N	\N	candidateExams.OET	What was your overall OET score?	\N
38	f	1	2025-06-30 12:24:38.701803+00	7	Please write your score below (numeric value)	\N	oetReadingScore	f	QuestionTask	\N	\N	\N	\N	\N	candidateExams.OETRead	What was your OET Reading score?	\N
39	f	1	2025-06-30 12:24:38.701803+00	7	Please write your score below (numeric value)	\N	oetListeningScore	f	QuestionTask	\N	\N	\N	\N	\N	candidateExams.OETList	What was your OET Listening score?	\N
40	f	1	2025-06-30 12:24:38.701803+00	7	Please write your score below (numeric value)	\N	oetLanguageScore	f	QuestionTask	\N	\N	\N	\N	\N	candidateExams.OETLang	What was your OET Language Knowledge score?	\N
41	f	1	2025-06-30 12:24:38.798654+00	7	Please select:	\N	studiedEnglishUniversity	f	QuestionTask	\N	\N	\N	\N	\N	\N	Did you study in English at University?	Yes,No
42	f	1	2025-06-30 12:24:38.81957+00	7	Please select: (if you do not have a spouse, please abandon the task and provide comment that you do not have a spouse)	\N	spouseJobUk	f	QuestionTask	\N	\N	\N	\N	\N	\N	Is your spouse also applying for a job in the UK?	Yes,No
43	f	1	2025-06-30 12:24:38.81957+00	7	Please select: (if no, please apply for a passport as soon as possible. If you need support with paying for funds)	\N	validPassport	f	QuestionTask	\N	\N	\N	\N	\N	\N	Do you have a valid passport (in date with no omissions of dates e.g. birth date, at least 6 months validity after arrival in UK)?	Yes,No
44	f	1	2025-06-30 12:24:38.81957+00	7	Please select: (if did not study in english or if have technical baccalaureate, need to book an English test)	\N	minBachelorsDegree	f	QuestionTask	\N	\N	\N	\N	\N	\N	Did you get at least a Bachelors degree?	Yes,No
45	f	1	2025-06-30 12:24:38.81957+00	7	Please select: (if maybe, please provide comment)	\N	colleaguesKnowRefugeeStatus	f	QuestionTask	\N	\N	\N	\N	\N	\N	Are you happy for colleagues to know you are a refugee/displaced talent at work?	Yes,No,Maybe
46	f	1	2025-06-30 12:24:38.81957+00	7	Please select: 	\N	ownLaptopUK	f	QuestionTask	\N	\N	\N	\N	\N	\N	Do you have a laptop you can bring to the UK?	Yes,No
47	f	1	2025-06-30 12:24:38.81957+00	7	Please select: (if maybe, please provide comment)	\N	shareDetailsTbbPartners	f	QuestionTask	\N	\N	\N	\N	\N	\N	Do you agree to TBB sharing your contact details (phone/ whatsapp number and email address) with our partners, including IOM and Reset? (We will only share your contact details if absolutely necessary for your relocation to the UK e.g. to book flights)	Yes,No,Maybe
48	f	1	2025-06-30 12:24:38.840577+00	7	Please select which option best describes your status:	\N	maritalStatus	f	QuestionTask	\N	\N	\N	\N	\N	maritalStatus	What is your marital status?	\N
49	f	1	2025-06-30 12:24:38.840577+00	7	Please write how many of each gender: (if no children put 0)	\N	childrenToUk	f	QuestionTask	\N	\N	\N	\N	\N	\N	How many children do you want to bring to the UK?	\N
50	f	1	2025-06-30 12:24:38.840577+00	7	Please select: (If not vaccinated, please check abandon task and provide details)	\N	covidVaccinatedStatus	f	QuestionTask	\N	\N	\N	\N	\N	covidVaccinatedStatus	Are you fully vaccinated (double dose) or partially vaccinated (1st dose only) against Covid-19?	\N
51	f	1	2025-06-30 12:24:38.840577+00	7	Please select: (If not vaccinated, please check abandon task and provide details)	\N	covidVaccinatedDate	f	QuestionTask	\N	\N	\N	\N	\N	covidVaccinatedDate	What was the date of your last Covid-19 vaccination?	\N
52	f	1	2025-06-30 12:24:38.840577+00	7	Please select: (If not vaccinated, please check abandon task and provide details)	\N	covidVaccinatedName	f	QuestionTask	\N	\N	\N	\N	\N	covidVaccineName	What was the name of your last Covid-19 vaccination?	\N
53	f	1	2025-06-30 12:24:38.840577+00	7	Please provide relationship (including if they are a TBB candidate) and location: (if no family/friends in UK abandon task and add comment)	\N	familyFriendsUkLoc	f	QuestionTask	\N	\N	\N	\N	\N	\N	If you have family/friends in the UK where are they located?	\N
54	f	1	2025-06-30 12:24:38.840577+00	7	Please select: (If yes, please add comment who and where they are in the UK)	\N	placedTbbCandidate	f	QuestionTask	\N	\N	\N	\N	\N	\N	Do you want to be placed with another TBB candidate?	Yes,No
55	f	1	2025-06-30 12:24:38.840577+00	7	Please put number of days/weeks/months: (if no notice period put 0)	\N	noticePeriod	f	QuestionTask	\N	\N	\N	\N	\N	\N	How long is your notice period?	\N
56	f	1	2025-06-30 12:24:38.840577+00	7	TBB often takes videos of airport arrivals, graduation ceremonies, interviews candidates for blog and news articles, etc. We share this media on social media pages including Linkedin, Twitter, and other platforms. Please select: (If maybe, please add comment what you are/are not ok with)	\N	mediaWillingness	f	QuestionTask	\N	\N	\N	\N	\N	mediaWillingness	Do you agree to be included in media (videos, articles and other media) that is shared by TBB and partners on social media?	\N
57	f	1	2025-06-30 12:24:38.877327+00	7	Research the employer and role prior to your interview.	\N	researchForInterview	f	Task	\N	\N	\N	\N	\N	\N	Research for interview	\N
58	f	1	2025-06-30 12:24:39.197077+00	7	Please enter a 10 digit number	\N	nmcPrn	f	QuestionTask	\N	\N	\N	\N	\N	\N	What is your NMC PRN (Nursing and Midwifery Council Personal Reference Number)?	\N
59	f	1	2025-06-30 12:24:41.996717+00	14	We are offering you a free Duolingo Test.	\N	duolingoTest	f	Task	\N	\N	\N	\N	\N	\N	Take the Duolingo English Test	\N
60	f	1	2025-06-30 12:24:42.096871+00	14	Claim your free coupon by clicking the 'Claim Coupon' button.	\N	claimCouponButton	f	Task	\N	\N	\N	\N	\N	\N	Claim the Duolingo Coupon	\N
\.


--
-- Data for Name: task_assignment; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.task_assignment (id, abandoned_date, activated_by, activated_date, candidate_id, candidate_notes, completed_date, deactivated_by, deactivated_date, due_date, related_list_id, status, task_id, task_type) FROM stdin;
\.


--
-- Data for Name: task_saved_list; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.task_saved_list (task_id, saved_list_id) FROM stdin;
\.


--
-- Data for Name: translation; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.translation (id, object_id, object_type, language, value, created_by, created_date, updated_by, updated_date) FROM stdin;
1	10000	language	ar	الأشولية	\N	\N	\N	\N
2	10001	language	ar	الأفريقانية	\N	\N	\N	\N
3	10002	language	ar	الأكانية	\N	\N	\N	\N
4	10003	language	ar	الألبانية	\N	\N	\N	\N
5	10004	language	ar	الأمهرية	\N	\N	\N	\N
6	10005	language	ar	الأشانتي	\N	\N	\N	\N
7	10006	language	ar	لغة الإشارة الأميركية	\N	\N	\N	\N
8	10007	language	ar	الآشورية	\N	\N	\N	\N
9	10008	language	ar	الأذربيجانية	\N	\N	\N	\N
10	10009	language	ar	الأذرية	\N	\N	\N	\N
11	10010	language	ar	الباجونية	\N	\N	\N	\N
12	10011	language	ar	الباسكية	\N	\N	\N	\N
13	10012	language	ar	البادينية	\N	\N	\N	\N
14	10013	language	ar	البيلاروسية	\N	\N	\N	\N
15	10014	language	ar	البنغالية	\N	\N	\N	\N
16	10015	language	ar	البربرية	\N	\N	\N	\N
17	10016	language	ar	البوسنية	\N	\N	\N	\N
18	10017	language	ar	البروانية	\N	\N	\N	\N
19	10018	language	ar	البلغارية	\N	\N	\N	\N
20	10019	language	ar	البورمية	\N	\N	\N	\N
21	10020	language	ar	الكاكشيكال	\N	\N	\N	\N
22	10021	language	ar	الكمبودية	\N	\N	\N	\N
23	10022	language	ar	الكانتونية	\N	\N	\N	\N
24	10023	language	ar	الكتالونية	\N	\N	\N	\N
25	10024	language	ar	الكلدانية	\N	\N	\N	\N
26	10025	language	ar	الشامورو	\N	\N	\N	\N
27	10026	language	ar	الشاوزو	\N	\N	\N	\N
28	10027	language	ar	الشباكانو	\N	\N	\N	\N
29	10028	language	ar	التشين	\N	\N	\N	\N
30	10029	language	ar	التشوك	\N	\N	\N	\N
31	10030	language	ar	الكري	\N	\N	\N	\N
32	10031	language	ar	الكرواتية	\N	\N	\N	\N
33	10032	language	ar	التشيكية	\N	\N	\N	\N
34	10033	language	ar	الداكوتا	\N	\N	\N	\N
35	10034	language	ar	الدانمركية	\N	\N	\N	\N
36	10035	language	ar	الدارية	\N	\N	\N	\N
37	10036	language	ar	الدينكا	\N	\N	\N	\N
38	10037	language	ar	الديولي	\N	\N	\N	\N
39	10038	language	ar	الهولندية	\N	\N	\N	\N
40	10039	language	ar	الإيدو	\N	\N	\N	\N
41	10040	language	ar	الإستونية	\N	\N	\N	\N
42	10041	language	ar	الإيوي	\N	\N	\N	\N
43	10042	language	ar	الفانتي	\N	\N	\N	\N
44	10043	language	ar	الفيجية الهندية	\N	\N	\N	\N
45	10044	language	ar	الفنلندية	\N	\N	\N	\N
46	10045	language	ar	الفلمنكية	\N	\N	\N	\N
47	10046	language	ar	الفرنسية الكندية	\N	\N	\N	\N
48	10047	language	ar	الفوجي	\N	\N	\N	\N
49	10048	language	ar	الفولا	\N	\N	\N	\N
50	10049	language	ar	الفولانية	\N	\N	\N	\N
51	10050	language	ar	الفوزهو	\N	\N	\N	\N
52	10051	language	ar	الغا	\N	\N	\N	\N
53	10052	language	ar	الغادانغ	\N	\N	\N	\N
54	10053	language	ar	الغايلية	\N	\N	\N	\N
55	10054	language	ar	الغايلية-الإيرلندية	\N	\N	\N	\N
56	10055	language	ar	الغايلية-الاسكتلندية	\N	\N	\N	\N
57	10056	language	ar	الجورجية	\N	\N	\N	\N
58	10057	language	ar	الغورانية	\N	\N	\N	\N
59	10058	language	ar	الغجراتية	\N	\N	\N	\N
60	10059	language	ar	الهايتية الكريولية	\N	\N	\N	\N
61	10060	language	ar	الهاكا	\N	\N	\N	\N
62	10061	language	ar	الهاكا-الصينية	\N	\N	\N	\N
63	10062	language	ar	الهوسا	\N	\N	\N	\N
64	10063	language	ar	العبرية	\N	\N	\N	\N
65	10064	language	ar	الهندية	\N	\N	\N	\N
66	10065	language	ar	الهمونغ	\N	\N	\N	\N
67	10066	language	ar	الهنغارية	\N	\N	\N	\N
68	10067	language	ar	الإيباناغ	\N	\N	\N	\N
69	10068	language	ar	الإيبو	\N	\N	\N	\N
70	10069	language	ar	الآيسلندية	\N	\N	\N	\N
71	10070	language	ar	الإغبو	\N	\N	\N	\N
72	10071	language	ar	الإيلوكانو	\N	\N	\N	\N
73	10072	language	ar	الأندونيسية	\N	\N	\N	\N
74	10073	language	ar	الإينوكتيتوت	\N	\N	\N	\N
75	10074	language	ar	الجاكرتية	\N	\N	\N	\N
76	10075	language	ar	اليابانية	\N	\N	\N	\N
77	10076	language	ar	الجاوية	\N	\N	\N	\N
78	10077	language	ar	الكانخوبال	\N	\N	\N	\N
79	10078	language	ar	الكارين	\N	\N	\N	\N
80	10079	language	ar	الكاريني	\N	\N	\N	\N
81	10080	language	ar	الكشميرية	\N	\N	\N	\N
82	10081	language	ar	الكازاخية	\N	\N	\N	\N
83	10082	language	ar	الكيكويو	\N	\N	\N	\N
84	10083	language	ar	الكينيارواندا	\N	\N	\N	\N
85	10084	language	ar	الكيروندية	\N	\N	\N	\N
86	10085	language	ar	الكورية	\N	\N	\N	\N
87	10086	language	ar	الكوسوفية	\N	\N	\N	\N
88	10087	language	ar	الكوتوكولية	\N	\N	\N	\N
89	10088	language	ar	الكريوية	\N	\N	\N	\N
90	10089	language	ar	الكردية	\N	\N	\N	\N
91	10090	language	ar	الكورمانجية	\N	\N	\N	\N
92	10091	language	ar	القرغيزية	\N	\N	\N	\N
93	10092	language	ar	اللاكوتا	\N	\N	\N	\N
94	10093	language	ar	اللاوية	\N	\N	\N	\N
95	10094	language	ar	اللاتفية	\N	\N	\N	\N
96	10095	language	ar	اللينغالا	\N	\N	\N	\N
97	10096	language	ar	الليتوانية	\N	\N	\N	\N
98	10097	language	ar	اللوغندية	\N	\N	\N	\N
99	10098	language	ar	اللوو	\N	\N	\N	\N
100	10099	language	ar	الماي	\N	\N	\N	\N
101	10100	language	ar	المقدونية	\N	\N	\N	\N
102	10101	language	ar	الملايوية	\N	\N	\N	\N
103	10102	language	ar	الماليالامية	\N	\N	\N	\N
104	10103	language	ar	المالطية	\N	\N	\N	\N
105	10104	language	ar	المندرينية	\N	\N	\N	\N
106	10105	language	ar	الماندينغو	\N	\N	\N	\N
107	10106	language	ar	المانيدينكا	\N	\N	\N	\N
108	10107	language	ar	الماراثية	\N	\N	\N	\N
109	10108	language	ar	المارشالية	\N	\N	\N	\N
110	10109	language	ar	الميان	\N	\N	\N	\N
111	10110	language	ar	المينا	\N	\N	\N	\N
112	10111	language	ar	الميربورية	\N	\N	\N	\N
113	10112	language	ar	الميكستيكية	\N	\N	\N	\N
114	10113	language	ar	المولدوفية	\N	\N	\N	\N
115	10114	language	ar	المونغولية	\N	\N	\N	\N
116	10115	language	ar	المونتنغرية	\N	\N	\N	\N
117	10116	language	ar	النافاجو	\N	\N	\N	\N
118	10117	language	ar	النابولية	\N	\N	\N	\N
119	10118	language	ar	النيبالية	\N	\N	\N	\N
120	10119	language	ar	النيجيرية الهجينة	\N	\N	\N	\N
121	10120	language	ar	النرويجية	\N	\N	\N	\N
122	10121	language	ar	الأورومو	\N	\N	\N	\N
123	10122	language	ar	البهارية	\N	\N	\N	\N
124	10123	language	ar	الباباغو	\N	\N	\N	\N
125	10124	language	ar	البابيامينتو	\N	\N	\N	\N
126	10125	language	ar	الباتوا المحلّية	\N	\N	\N	\N
127	10126	language	ar	الانكليزية الهجينة	\N	\N	\N	\N
128	10127	language	ar	البولندية	\N	\N	\N	\N
129	10128	language	ar	البرتغالية الكريولية	\N	\N	\N	\N
130	10129	language	ar	البوتهوارية	\N	\N	\N	\N
131	10130	language	ar	البولارية	\N	\N	\N	\N
132	10131	language	ar	البنجابية	\N	\N	\N	\N
133	10132	language	ar	البوتيانية	\N	\N	\N	\N
134	10133	language	ar	الكيتشوا	\N	\N	\N	\N
135	10134	language	ar	الرومانية	\N	\N	\N	\N
136	10135	language	ar	الساموية	\N	\N	\N	\N
137	10136	language	ar	الصربية	\N	\N	\N	\N
138	10137	language	ar	لهجة الشانغهاي	\N	\N	\N	\N
139	10138	language	ar	الشونا	\N	\N	\N	\N
140	10139	language	ar	السيتشوانية	\N	\N	\N	\N
141	10140	language	ar	الصقلية	\N	\N	\N	\N
142	10141	language	ar	السنهالية	\N	\N	\N	\N
143	10142	language	ar	السلوفاكية	\N	\N	\N	\N
144	10143	language	ar	السورانية	\N	\N	\N	\N
145	10144	language	ar	العربية السودانية	\N	\N	\N	\N
146	10145	language	ar	السوسو	\N	\N	\N	\N
147	10146	language	ar	السواحلية	\N	\N	\N	\N
148	10147	language	ar	السويدية	\N	\N	\N	\N
149	10148	language	ar	السيلهيتية	\N	\N	\N	\N
150	10149	language	ar	التاغالوغ	\N	\N	\N	\N
151	10150	language	ar	التايوانية	\N	\N	\N	\N
152	10151	language	ar	الطاجيكية	\N	\N	\N	\N
153	10152	language	ar	التاميلية	\N	\N	\N	\N
154	10153	language	ar	التيلوفو	\N	\N	\N	\N
155	10154	language	ar	التايلندية	\N	\N	\N	\N
156	10155	language	ar	التيبتية	\N	\N	\N	\N
157	10156	language	ar	التجرية	\N	\N	\N	\N
158	10157	language	ar	التيغرينية	\N	\N	\N	\N
159	10158	language	ar	التايشانية	\N	\N	\N	\N
160	10159	language	ar	التونغية	\N	\N	\N	\N
161	10160	language	ar	التكرورية	\N	\N	\N	\N
162	10161	language	ar	التريكية	\N	\N	\N	\N
163	10162	language	ar	التشيلوبا	\N	\N	\N	\N
164	10163	language	ar	التوي	\N	\N	\N	\N
165	10164	language	ar	الأوكرانية	\N	\N	\N	\N
166	10165	language	ar	الأردية	\N	\N	\N	\N
167	10166	language	ar	الأويغورية	\N	\N	\N	\N
168	10167	language	ar	الأوزبكية	\N	\N	\N	\N
169	10168	language	ar	الفيتنامية	\N	\N	\N	\N
170	10169	language	ar	الفيسايانية	\N	\N	\N	\N
171	10170	language	ar	الويلزية	\N	\N	\N	\N
172	10171	language	ar	الولوفية	\N	\N	\N	\N
173	10172	language	ar	اليديشية	\N	\N	\N	\N
174	10173	language	ar	اليوروبية	\N	\N	\N	\N
175	10174	language	ar	اليوبيكية	\N	\N	\N	\N
176	1	survey_type	ar	جلسة معلومات	\N	\N	\N	\N
177	2	survey_type	ar	منشورات من مراكز خدمات اجتماعية	\N	\N	\N	\N
178	3	survey_type	ar	من خلال صديق	\N	\N	\N	\N
179	4	survey_type	ar	فيسبوك- من خلال صديق	\N	\N	\N	\N
180	5	survey_type	ar	فيسبوك- من خلال منظمة	\N	\N	\N	\N
181	6	survey_type	ar	مستشار أو متطوع خدمات اجتماعية	\N	\N	\N	\N
182	7	survey_type	ar	منظمات و جمعيات غير الحكومية	\N	\N	\N	\N
183	8	survey_type	ar	آخر	\N	\N	\N	\N
\.


--
-- Data for Name: user_job; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.user_job (user_id, tc_job_id) FROM stdin;
\.


--
-- Data for Name: user_saved_list; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.user_saved_list (user_id, saved_list_id) FROM stdin;
\.


--
-- Data for Name: user_saved_search; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.user_saved_search (user_id, saved_search_id) FROM stdin;
\.


--
-- Data for Name: user_source_country; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.user_source_country (user_id, country_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: tctalent
--

COPY public.users (id, username, first_name, last_name, email, role, status, password_enc, last_login, created_by, created_date, updated_by, updated_date, reset_token, reset_token_issued_date, password_updated_date, read_only, using_mfa, mfa_secret, host_domain, partner_id, approver_id, purpose, job_creator, email_verified, email_verification_token, email_verification_token_issued_time) FROM stdin;
1	SystemAdmin	System	Admin	tbbtalent@talentbeyondboundaries.org	systemadmin	active	\N	\N	\N	\N	\N	\N	\N	\N	\N	f	f	\N	\N	1	\N	\N	f	f	\N	\N
\.


--
-- Name: audit_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.audit_log_id_seq', 1, false);


--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_attachment_id_seq', 10000, false);


--
-- Name: candidate_certification_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_certification_id_seq', 20000, false);


--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_citizenship_id_seq', 1, false);


--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_coupon_code_id_seq', 1, false);


--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_dependant_id_seq', 1, false);


--
-- Name: candidate_destination_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_destination_id_seq', 1, false);


--
-- Name: candidate_education_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_education_id_seq', 10000, false);


--
-- Name: candidate_exam_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_exam_id_seq', 1, false);


--
-- Name: candidate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_id_seq', 20000, false);


--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_job_experience_id_seq', 25000, false);


--
-- Name: candidate_language_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_language_id_seq', 25000, false);


--
-- Name: candidate_note_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_note_id_seq', 10000, false);


--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_occupation_id_seq', 20000, false);


--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_opportunity_id_seq', 1, false);


--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_review_item_id_seq', 1, false);


--
-- Name: candidate_skill_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_skill_id_seq', 27000, false);


--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_visa_check_id_seq', 1, false);


--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.candidate_visa_job_check_id_seq', 1, false);


--
-- Name: chat_post_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.chat_post_id_seq', 1, false);


--
-- Name: country_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.country_id_seq', 10000, false);


--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.duolingo_coupon_id_seq', 1, false);


--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.duolingo_extra_fields_id_seq', 1, false);


--
-- Name: education_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.education_level_id_seq', 10000, false);


--
-- Name: education_major_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.education_major_id_seq', 10000, false);


--
-- Name: employer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.employer_id_seq', 1, false);


--
-- Name: export_column_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.export_column_id_seq', 1, false);


--
-- Name: help_link_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.help_link_id_seq', 58, true);


--
-- Name: industry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.industry_id_seq', 10000, false);


--
-- Name: job_chat_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.job_chat_id_seq', 1, false);


--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.job_opp_intake_id_seq', 1, false);


--
-- Name: language_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.language_id_seq', 10188, true);


--
-- Name: language_level_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.language_level_id_seq', 500, false);


--
-- Name: link_preview_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.link_preview_id_seq', 1, false);


--
-- Name: nationality_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.nationality_id_seq', 10000, false);


--
-- Name: occupation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.occupation_id_seq', 10021, true);


--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.offer_to_assist_id_seq', 1, false);


--
-- Name: partner_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.partner_id_seq', 9, true);


--
-- Name: reaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.reaction_id_seq', 1, false);


--
-- Name: root_request_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.root_request_id_seq', 1, false);


--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.salesforce_job_opp_tc_job_id_seq', 1, false);


--
-- Name: saved_list_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.saved_list_id_seq', 1, true);


--
-- Name: saved_search_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.saved_search_id_seq', 1, false);


--
-- Name: search_join_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.search_join_id_seq', 1, false);


--
-- Name: survey_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.survey_type_id_seq', 23, true);


--
-- Name: system_language_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.system_language_id_seq', 3, true);


--
-- Name: task_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.task_assignment_id_seq', 1, false);


--
-- Name: task_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.task_id_seq', 60, true);


--
-- Name: translation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.translation_id_seq', 183, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: tctalent
--

SELECT pg_catalog.setval('public.users_id_seq', 25000, false);


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: candidate_certification candidate_certification_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_certification
    ADD CONSTRAINT candidate_certification_pkey PRIMARY KEY (id);


--
-- Name: candidate_citizenship candidate_citizenship_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_pkey PRIMARY KEY (id);


--
-- Name: candidate_coupon_code candidate_coupon_code_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_pkey PRIMARY KEY (id);


--
-- Name: candidate_dependant candidate_dependant_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_dependant
    ADD CONSTRAINT candidate_dependant_pkey PRIMARY KEY (id);


--
-- Name: candidate_destination candidate_destination_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_pkey PRIMARY KEY (id);


--
-- Name: candidate_education candidate_education_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_pkey PRIMARY KEY (id);


--
-- Name: candidate_exam candidate_exam_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_exam
    ADD CONSTRAINT candidate_exam_pkey PRIMARY KEY (id);


--
-- Name: candidate_attachment candidate_file_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_pkey PRIMARY KEY (id);


--
-- Name: candidate_job_experience candidate_job_experience_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_pkey PRIMARY KEY (id);


--
-- Name: candidate_language candidate_language_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_pkey PRIMARY KEY (id);


--
-- Name: candidate_note candidate_note_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_pkey PRIMARY KEY (id);


--
-- Name: candidate_occupation candidate_occupation_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_pkey PRIMARY KEY (id);


--
-- Name: candidate_opportunity candidate_opportunity_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_pkey PRIMARY KEY (id);


--
-- Name: candidate candidate_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_pkey PRIMARY KEY (id);


--
-- Name: candidate_property candidate_property_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_pkey PRIMARY KEY (candidate_id, name);


--
-- Name: candidate_review_item candidate_review_item_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_pkey PRIMARY KEY (id);


--
-- Name: candidate_visa_job_check candidate_role_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_role_pkey PRIMARY KEY (id);


--
-- Name: candidate_saved_list candidate_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_pkey PRIMARY KEY (candidate_id, saved_list_id);


--
-- Name: candidate_skill candidate_skill_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_skill
    ADD CONSTRAINT candidate_skill_pkey PRIMARY KEY (id);


--
-- Name: candidate_visa_check candidate_visa_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_pkey PRIMARY KEY (id);


--
-- Name: chat_post chat_post_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_pkey PRIMARY KEY (id);


--
-- Name: country_nationality_join country_nationality_join_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_pkey PRIMARY KEY (country_id, nationality_id);


--
-- Name: country country_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_pkey PRIMARY KEY (id);


--
-- Name: duolingo_coupon duolingo_coupon_coupon_code_key; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT duolingo_coupon_coupon_code_key UNIQUE (coupon_code);


--
-- Name: duolingo_coupon duolingo_coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT duolingo_coupon_pkey PRIMARY KEY (id);


--
-- Name: duolingo_extra_fields duolingo_extra_fields_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_extra_fields
    ADD CONSTRAINT duolingo_extra_fields_pkey PRIMARY KEY (id);


--
-- Name: education_level education_level_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.education_level
    ADD CONSTRAINT education_level_pkey PRIMARY KEY (id);


--
-- Name: education_major education_major_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.education_major
    ADD CONSTRAINT education_major_pkey PRIMARY KEY (id);


--
-- Name: employer employer_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_pkey PRIMARY KEY (id);


--
-- Name: export_column export_column_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: help_link help_link_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_pkey PRIMARY KEY (id);


--
-- Name: industry industry_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.industry
    ADD CONSTRAINT industry_pkey PRIMARY KEY (id);


--
-- Name: job_chat job_chat_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_pkey PRIMARY KEY (id);


--
-- Name: job_chat_user job_chat_user_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_pkey PRIMARY KEY (job_chat_id, user_id);


--
-- Name: job_opp_intake job_opp_intake_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_opp_intake
    ADD CONSTRAINT job_opp_intake_pkey PRIMARY KEY (id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_pkey PRIMARY KEY (tc_job_id, saved_search_id);


--
-- Name: language_level language_level_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.language_level
    ADD CONSTRAINT language_level_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: link_preview link_preview_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.link_preview
    ADD CONSTRAINT link_preview_pkey PRIMARY KEY (id);


--
-- Name: nationality nationality_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.nationality
    ADD CONSTRAINT nationality_pkey PRIMARY KEY (id);


--
-- Name: occupation occupation_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.occupation
    ADD CONSTRAINT occupation_pkey PRIMARY KEY (id);


--
-- Name: offer_to_assist offer_to_assist_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_pkey PRIMARY KEY (id);


--
-- Name: partner_job partner_job_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_pkey PRIMARY KEY (partner_id, tc_job_id);


--
-- Name: partner partner_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_pkey PRIMARY KEY (id);


--
-- Name: partner_source_country partner_source_country_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_pkey PRIMARY KEY (partner_id, country_id);


--
-- Name: reaction reaction_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_pkey PRIMARY KEY (id);


--
-- Name: reaction_user reaction_user_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_pkey PRIMARY KEY (reaction_id, user_id);


--
-- Name: root_request root_request_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.root_request
    ADD CONSTRAINT root_request_pkey PRIMARY KEY (id);


--
-- Name: salesforce_job_opp salesforce_job_opp_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_pkey PRIMARY KEY (id);


--
-- Name: saved_list saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_pkey PRIMARY KEY (id);


--
-- Name: saved_search saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_pkey PRIMARY KEY (id);


--
-- Name: search_join search_join_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_pkey PRIMARY KEY (id);


--
-- Name: shedlock shedlock_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.shedlock
    ADD CONSTRAINT shedlock_pkey PRIMARY KEY (name);


--
-- Name: survey_type survey_type_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.survey_type
    ADD CONSTRAINT survey_type_pkey PRIMARY KEY (id);


--
-- Name: system_language system_language_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_pkey PRIMARY KEY (id);


--
-- Name: task_assignment task_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_pkey PRIMARY KEY (id);


--
-- Name: task task_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_pkey PRIMARY KEY (id);


--
-- Name: task_saved_list task_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_pkey PRIMARY KEY (saved_list_id, task_id);


--
-- Name: translation translation_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_pkey PRIMARY KEY (id);


--
-- Name: candidate_occupation uq_candidate_occupation; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT uq_candidate_occupation UNIQUE (candidate_id, occupation_id);


--
-- Name: salesforce_job_opp uq_tc_job_id; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT uq_tc_job_id UNIQUE (id);


--
-- Name: candidate uq_user_id; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT uq_user_id UNIQUE (user_id);


--
-- Name: user_job user_job_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_pkey PRIMARY KEY (user_id, tc_job_id);


--
-- Name: user_saved_list user_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_pkey PRIMARY KEY (user_id, saved_list_id);


--
-- Name: user_saved_search user_saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_pkey PRIMARY KEY (user_id, saved_search_id);


--
-- Name: user_source_country user_source_country_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_pkey PRIMARY KEY (user_id, country_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: candidate_candidate_number_uindex; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE UNIQUE INDEX candidate_candidate_number_uindex ON public.candidate USING btree (candidate_number);


--
-- Name: candidate_dependant_candidate_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_dependant_candidate_id_idx ON public.candidate_dependant USING btree (candidate_id);


--
-- Name: candidate_number_status_country_active_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_number_status_country_active_idx ON public.candidate USING btree (candidate_number, status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_public_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_public_id_idx ON public.candidate USING btree (public_id);


--
-- Name: candidate_registered_by_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_registered_by_idx ON public.candidate USING btree (registered_by);


--
-- Name: candidate_saved_list_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_saved_list_id_idx ON public.candidate_saved_list USING btree (saved_list_id);


--
-- Name: candidate_sflink_index; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_sflink_index ON public.candidate USING btree (sflink);


--
-- Name: candidate_status_index; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_status_index ON public.candidate USING btree (status);


--
-- Name: candidate_user_status_country_active_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX candidate_user_status_country_active_idx ON public.candidate USING btree (user_id, status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: country_iso_code_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX country_iso_code_idx ON public.country USING btree (iso_code);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_candidate_external_id_country; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX idx_candidate_external_id_country ON public.candidate USING btree (lower(external_id), country_id);


--
-- Name: idx_candidate_phone_lower; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX idx_candidate_phone_lower ON public.candidate USING btree (lower(phone)) WHERE (status <> 'deleted'::text);


--
-- Name: idx_candidate_status_country; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX idx_candidate_status_country ON public.candidate USING btree (status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: idx_candidate_whatsapp_lower; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX idx_candidate_whatsapp_lower ON public.candidate USING btree (lower(whatsapp)) WHERE (status <> 'deleted'::text);


--
-- Name: idx_users_email_lower; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX idx_users_email_lower ON public.users USING btree (lower(email));


--
-- Name: ip_address_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX ip_address_idx ON public.root_request USING btree (ip_address);


--
-- Name: job_chat_candidate_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX job_chat_candidate_id_idx ON public.job_chat USING btree (candidate_id);


--
-- Name: job_chat_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX job_chat_id_idx ON public.chat_post USING btree (job_chat_id);


--
-- Name: job_chat_job_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX job_chat_job_id_idx ON public.job_chat USING btree (job_id);


--
-- Name: job_chat_source_partner_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX job_chat_source_partner_id_idx ON public.job_chat USING btree (source_partner_id);


--
-- Name: job_chat_type_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX job_chat_type_idx ON public.job_chat USING btree (type);


--
-- Name: language_iso_code_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX language_iso_code_idx ON public.language USING btree (iso_code);


--
-- Name: occupation_isco08_code_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX occupation_isco08_code_idx ON public.occupation USING btree (isco08_code);


--
-- Name: offer_to_assist_public_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX offer_to_assist_public_id_idx ON public.offer_to_assist USING btree (public_id);


--
-- Name: partner_public_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX partner_public_id_idx ON public.partner USING btree (public_id);


--
-- Name: salesforce_job_opp_tc_job_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX salesforce_job_opp_tc_job_id_idx ON public.salesforce_job_opp USING btree (id);


--
-- Name: saved_list_public_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX saved_list_public_id_idx ON public.saved_list USING btree (public_id);


--
-- Name: saved_search_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX saved_search_id_idx ON public.user_saved_search USING btree (saved_search_id);


--
-- Name: saved_search_public_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX saved_search_public_id_idx ON public.saved_search USING btree (public_id);


--
-- Name: user_job_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX user_job_id_idx ON public.user_job USING btree (tc_job_id);


--
-- Name: user_lower_username_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX user_lower_username_idx ON public.users USING btree (lower(username));


--
-- Name: user_saved_list_id_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX user_saved_list_id_idx ON public.user_saved_list USING btree (saved_list_id);


--
-- Name: users_lower_first_name_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX users_lower_first_name_idx ON public.users USING btree (lower(first_name)) WHERE (status <> 'deleted'::text);


--
-- Name: users_lower_last_name_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX users_lower_last_name_idx ON public.users USING btree (lower(last_name)) WHERE (status <> 'deleted'::text);


--
-- Name: users_partner_id_index; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX users_partner_id_index ON public.users USING btree (partner_id);


--
-- Name: users_username_active_status_idx; Type: INDEX; Schema: public; Owner: tctalent
--

CREATE INDEX users_username_active_status_idx ON public.users USING btree (lower(username)) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_attachment candidate_attachment_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_attachment_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_birth_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_birth_country_id_fkey FOREIGN KEY (birth_country_id) REFERENCES public.country(id);


--
-- Name: candidate_certification candidate_certification_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_certification
    ADD CONSTRAINT candidate_certification_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_citizenship candidate_citizenship_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_citizenship candidate_citizenship_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_coupon_code candidate_coupon_code_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_coupon_code candidate_coupon_code_offer_to_assist_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_offer_to_assist_id_fkey FOREIGN KEY (offer_to_assist_id) REFERENCES public.offer_to_assist(id);


--
-- Name: candidate candidate_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_dependant candidate_dependant_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_dependant
    ADD CONSTRAINT candidate_dependant_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_destination candidate_destination_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_destination candidate_destination_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_driving_license_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_driving_license_country_id_fkey FOREIGN KEY (driving_license_country_id) REFERENCES public.country(id);


--
-- Name: candidate_education candidate_education_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_education candidate_education_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_education candidate_education_major_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_major_id_fkey FOREIGN KEY (major_id) REFERENCES public.education_major(id);


--
-- Name: candidate_exam candidate_exam_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_exam
    ADD CONSTRAINT candidate_exam_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_attachment candidate_file_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_attachment candidate_file_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_full_intake_completed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_full_intake_completed_by_fkey FOREIGN KEY (full_intake_completed_by) REFERENCES public.users(id);


--
-- Name: candidate_job_experience candidate_job_experience_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_job_experience candidate_job_experience_candidate_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_candidate_occupation_id_fkey FOREIGN KEY (candidate_occupation_id) REFERENCES public.candidate_occupation(id);


--
-- Name: candidate_job_experience candidate_job_experience_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_language candidate_language_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_language candidate_language_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_language_id_fkey FOREIGN KEY (language_id) REFERENCES public.language(id);


--
-- Name: candidate_language candidate_language_spoken_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_spoken_level_id_fkey FOREIGN KEY (spoken_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate_language candidate_language_written_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_written_level_id_fkey FOREIGN KEY (written_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate candidate_max_education_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_max_education_level_id_fkey FOREIGN KEY (max_education_level_id) REFERENCES public.education_level(id);


--
-- Name: candidate candidate_mini_intake_completed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_mini_intake_completed_by_fkey FOREIGN KEY (mini_intake_completed_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_nationalityold_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_nationalityold_id_fkey FOREIGN KEY (nationalityold_id) REFERENCES public.nationality(id);


--
-- Name: candidate_note candidate_note_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_note candidate_note_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_note candidate_note_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_occupation candidate_occupation_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_occupation candidate_occupation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_occupation candidate_occupation_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_occupation_id_fkey FOREIGN KEY (occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_occupation candidate_occupation_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_opportunity candidate_opportunity_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_opportunity candidate_opportunity_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_opportunity candidate_opportunity_job_opp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_job_opp_id_fkey FOREIGN KEY (job_opp_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: candidate_opportunity candidate_opportunity_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_partner_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_candidate_id_fkey FOREIGN KEY (partner_candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate candidate_partner_edu_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_edu_level_id_fkey FOREIGN KEY (partner_edu_level_id) REFERENCES public.education_level(id);


--
-- Name: candidate candidate_partner_english_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_english_level_id_fkey FOREIGN KEY (partner_english_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate candidate_partner_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_occupation_id_fkey FOREIGN KEY (partner_occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_property candidate_property_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_property candidate_property_related_task_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_related_task_assignment_id_fkey FOREIGN KEY (related_task_assignment_id) REFERENCES public.task_assignment(id);


--
-- Name: candidate candidate_registered_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_registered_by_fkey FOREIGN KEY (registered_by) REFERENCES public.partner(id);


--
-- Name: candidate candidate_relocated_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_relocated_country_id_fkey FOREIGN KEY (relocated_country_id) REFERENCES public.country(id);


--
-- Name: candidate_review_item candidate_review_item_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_review_item candidate_review_item_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_review_item candidate_review_item_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: candidate_review_item candidate_review_item_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_visa_job_check candidate_role_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_role_occupation_id_fkey FOREIGN KEY (occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_saved_list candidate_saved_list_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_saved_list candidate_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: candidate_saved_list candidate_saved_list_shareable_cv_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_shareable_cv_attachment_id_fkey FOREIGN KEY (shareable_cv_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate_saved_list candidate_saved_list_shareable_doc_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_shareable_doc_attachment_id_fkey FOREIGN KEY (shareable_doc_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate candidate_shareable_cv_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_shareable_cv_attachment_id_fkey FOREIGN KEY (shareable_cv_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate candidate_shareable_doc_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_shareable_doc_attachment_id_fkey FOREIGN KEY (shareable_doc_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate_skill candidate_skill_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_skill
    ADD CONSTRAINT candidate_skill_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate candidate_survey_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_survey_type_id_fkey FOREIGN KEY (survey_type_id) REFERENCES public.survey_type(id);


--
-- Name: candidate candidate_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: candidate_visa_check candidate_visa_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_visa_check candidate_visa_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_visa_check candidate_visa_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_candidate_visa_check_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_candidate_visa_check_id_fkey FOREIGN KEY (candidate_visa_check_id) REFERENCES public.candidate_visa_check(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_job_opp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_job_opp_id_fkey FOREIGN KEY (job_opp_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: candidate_visa_check candidate_visa_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: chat_post chat_post_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: chat_post chat_post_job_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_job_chat_id_fkey FOREIGN KEY (job_chat_id) REFERENCES public.job_chat(id);


--
-- Name: chat_post chat_post_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: country_nationality_join country_nationality_join_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: country_nationality_join country_nationality_join_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.nationality(id);


--
-- Name: duolingo_extra_fields duolingo_extra_fields_candidate_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_extra_fields
    ADD CONSTRAINT duolingo_extra_fields_candidate_exam_id_fkey FOREIGN KEY (candidate_exam_id) REFERENCES public.candidate_exam(id);


--
-- Name: employer employer_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: employer employer_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: employer employer_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: export_column export_column_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: export_column export_column_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: duolingo_coupon fk_candidate; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: help_link help_link_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: help_link help_link_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: help_link help_link_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: job_chat job_chat_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: job_chat job_chat_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: job_chat job_chat_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: job_chat job_chat_source_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_source_partner_id_fkey FOREIGN KEY (source_partner_id) REFERENCES public.partner(id);


--
-- Name: job_chat job_chat_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: job_chat_user job_chat_user_job_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_job_chat_id_fkey FOREIGN KEY (job_chat_id) REFERENCES public.job_chat(id);


--
-- Name: job_chat_user job_chat_user_last_read_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_last_read_post_id_fkey FOREIGN KEY (last_read_post_id) REFERENCES public.chat_post(id);


--
-- Name: job_chat_user job_chat_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: link_preview link_preview_chat_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.link_preview
    ADD CONSTRAINT link_preview_chat_post_id_fkey FOREIGN KEY (chat_post_id) REFERENCES public.chat_post(id);


--
-- Name: offer_to_assist offer_to_assist_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: offer_to_assist offer_to_assist_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: offer_to_assist offer_to_assist_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: partner partner_default_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_default_contact_id_fkey FOREIGN KEY (default_contact_id) REFERENCES public.users(id);


--
-- Name: partner partner_employer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES public.employer(id);


--
-- Name: partner_job partner_job_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.users(id);


--
-- Name: partner_job partner_job_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: partner_job partner_job_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: partner partner_redirect_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_redirect_partner_id_fkey FOREIGN KEY (redirect_partner_id) REFERENCES public.partner(id);


--
-- Name: partner_source_country partner_source_country_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: partner_source_country partner_source_country_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: reaction reaction_chat_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_chat_post_id_fkey FOREIGN KEY (chat_post_id) REFERENCES public.chat_post(id);


--
-- Name: reaction_user reaction_user_reaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_reaction_id_fkey FOREIGN KEY (reaction_id) REFERENCES public.reaction(id);


--
-- Name: reaction_user reaction_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_contact_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_country_object_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_country_object_id_fkey FOREIGN KEY (country_object_id) REFERENCES public.country(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_employer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES public.employer(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_evergreen_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_evergreen_child_id_fkey FOREIGN KEY (evergreen_child_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_exclusion_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_exclusion_list_id_fkey FOREIGN KEY (exclusion_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_job_opp_intake_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_job_opp_intake_id_fkey FOREIGN KEY (job_opp_intake_id) REFERENCES public.job_opp_intake(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_published_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_published_by_fkey FOREIGN KEY (published_by) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_recruiter_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_recruiter_partner_id_fkey FOREIGN KEY (recruiter_partner_id) REFERENCES public.partner(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_submission_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_submission_list_id_fkey FOREIGN KEY (submission_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_suggested_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_suggested_list_id_fkey FOREIGN KEY (suggested_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: saved_list saved_list_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: saved_list saved_list_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: saved_list saved_list_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: saved_list saved_list_saved_search_source_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_saved_search_source_id_fkey FOREIGN KEY (saved_search_source_id) REFERENCES public.saved_search(id);


--
-- Name: saved_list saved_list_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: saved_search saved_search_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: saved_search saved_search_default_save_selection_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_default_save_selection_list_id_fkey FOREIGN KEY (default_save_selection_list_id) REFERENCES public.saved_list(id);


--
-- Name: saved_search saved_search_exclusion_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_exclusion_list_id_fkey FOREIGN KEY (exclusion_list_id) REFERENCES public.saved_list(id);


--
-- Name: saved_search saved_search_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: saved_search saved_search_other_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_other_language_id_fkey FOREIGN KEY (other_language_id) REFERENCES public.language(id);


--
-- Name: saved_search saved_search_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: search_join search_join_child_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_child_search_id_fkey FOREIGN KEY (child_search_id) REFERENCES public.saved_search(id);


--
-- Name: search_join search_join_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_search_id_fkey FOREIGN KEY (search_id) REFERENCES public.saved_search(id);


--
-- Name: system_language system_language_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: system_language system_language_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_activated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_activated_by_fkey FOREIGN KEY (activated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: task_assignment task_assignment_deactivated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_deactivated_by_fkey FOREIGN KEY (deactivated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_related_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_related_list_id_fkey FOREIGN KEY (related_list_id) REFERENCES public.saved_list(id);


--
-- Name: task_assignment task_assignment_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: task task_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: task_saved_list task_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: task_saved_list task_saved_list_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: task task_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: translation translation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: translation translation_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: user_job user_job_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: user_job user_job_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_saved_list user_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: user_saved_list user_saved_list_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_saved_search user_saved_search_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: user_saved_search user_saved_search_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_source_country user_source_country_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: user_source_country user_source_country_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: users users_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: users users_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: users users_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: tctalent
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: tctalent
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

