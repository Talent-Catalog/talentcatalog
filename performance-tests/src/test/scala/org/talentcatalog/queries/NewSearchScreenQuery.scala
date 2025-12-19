/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.talentcatalog.queries

import io.gatling.core.Predef._
import ru.tinkoff.load.jdbc.Predef._
import ru.tinkoff.load.jdbc.actions.actions._

object NewSearchScreenQuery {

  val newSearchScreenQueryName: String = "New Search Screen Query"

  // nes search screen paged query
  val newSearchPaged: String =
    """
      |select
      |        distinct candidate0_.id as id1_1_0_,
      |        user1_.id as id1_49_1_,
      |        partnerimp2_.id as id1_30_2_,
      |        country3_.id as id1_20_3_,
      |        country4_.id as id1_20_4_,
      |        educationl5_.id as id1_21_5_,
      |        candidate0_.created_by as create132_1_0_,
      |        candidate0_.created_date as created_2_1_0_,
      |        candidate0_.updated_by as update133_1_0_,
      |        candidate0_.updated_date as updated_3_1_0_,
      |        candidate0_.additional_info as addition4_1_0_,
      |        candidate0_.address1 as address5_1_0_,
      |        candidate0_.asylum_year as asylum_y6_1_0_,
      |        candidate0_.avail_immediate as avail_im7_1_0_,
      |        candidate0_.avail_immediate_job_ops as avail_im8_1_0_,
      |        candidate0_.avail_immediate_notes as avail_im9_1_0_,
      |        candidate0_.avail_immediate_reason as avail_i10_1_0_,
      |        candidate0_.birth_country_id as birth_134_1_0_,
      |        candidate0_.can_drive as can_dri11_1_0_,
      |        candidate0_.candidate_message as candida12_1_0_,
      |        candidate0_.candidate_number as candida13_1_0_,
      |        candidate0_.city as city14_1_0_,
      |        candidate0_.conflict as conflic15_1_0_,
      |        candidate0_.conflict_notes as conflic16_1_0_,
      |        candidate0_.country_id as countr135_1_0_,
      |        candidate0_.covid_vaccinated as covid_v17_1_0_,
      |        candidate0_.covid_vaccinated_date as covid_v18_1_0_,
      |        candidate0_.covid_vaccinated_status as covid_v19_1_0_,
      |        candidate0_.covid_vaccine_name as covid_v20_1_0_,
      |        candidate0_.covid_vaccine_notes as covid_v21_1_0_,
      |        candidate0_.crime_convict as crime_c22_1_0_,
      |        candidate0_.crime_convict_notes as crime_c23_1_0_,
      |        candidate0_.dest_limit as dest_li24_1_0_,
      |        candidate0_.dest_limit_notes as dest_li25_1_0_,
      |        candidate0_.dob as dob26_1_0_,
      |        candidate0_.driving_license as driving27_1_0_,
      |        candidate0_.driving_license_country_id as drivin136_1_0_,
      |        candidate0_.driving_license_exp as driving28_1_0_,
      |        candidate0_.external_id as externa29_1_0_,
      |        candidate0_.external_id_source as externa30_1_0_,
      |        candidate0_.family_move as family_31_1_0_,
      |        candidate0_.family_move_notes as family_32_1_0_,
      |        candidate0_.folderlink as folderl33_1_0_,
      |        candidate0_.folderlink_address as folderl34_1_0_,
      |        candidate0_.folderlink_character as folderl35_1_0_,
      |        candidate0_.folderlink_employer as folderl36_1_0_,
      |        candidate0_.folderlink_engagement as folderl37_1_0_,
      |        candidate0_.folderlink_experience as folderl38_1_0_,
      |        candidate0_.folderlink_family as folderl39_1_0_,
      |        candidate0_.folderlink_identity as folderl40_1_0_,
      |        candidate0_.folderlink_immigration as folderl41_1_0_,
      |        candidate0_.folderlink_language as folderl42_1_0_,
      |        candidate0_.folderlink_medical as folderl43_1_0_,
      |        candidate0_.folderlink_qualification as folderl44_1_0_,
      |        candidate0_.folderlink_registration as folderl45_1_0_,
      |        candidate0_.gender as gender46_1_0_,
      |        candidate0_.health_issues as health_47_1_0_,
      |        candidate0_.health_issues_notes as health_48_1_0_,
      |        candidate0_.home_location as home_lo49_1_0_,
      |        candidate0_.host_challenges as host_ch50_1_0_,
      |        candidate0_.host_entry_legally as host_en51_1_0_,
      |        candidate0_.host_entry_legally_notes as host_en52_1_0_,
      |        candidate0_.host_entry_year as host_en53_1_0_,
      |        candidate0_.host_entry_year_notes as host_en54_1_0_,
      |        candidate0_.ielts_score as ielts_s55_1_0_,
      |        candidate0_.int_recruit_other as int_rec56_1_0_,
      |        candidate0_.int_recruit_reasons as int_rec57_1_0_,
      |        candidate0_.int_recruit_rural as int_rec58_1_0_,
      |        candidate0_.int_recruit_rural_notes as int_rec59_1_0_,
      |        candidate0_.lang_assessment as lang_as60_1_0_,
      |        candidate0_.lang_assessment_score as lang_as61_1_0_,
      |        candidate0_.left_home_notes as left_ho62_1_0_,
      |        candidate0_.left_home_reasons as left_ho63_1_0_,
      |        candidate0_.linked_in_link as linked_64_1_0_,
      |        candidate0_.marital_status as marital65_1_0_,
      |        candidate0_.marital_status_notes as marital66_1_0_,
      |        candidate0_.max_education_level_id as max_ed137_1_0_,
      |        candidate0_.media_willingness as media_w67_1_0_,
      |        candidate0_.migration_education_major_id as migrat138_1_0_,
      |        candidate0_.migration_nationality as migrati68_1_0_,
      |        candidate0_.military_end as militar69_1_0_,
      |        candidate0_.military_notes as militar70_1_0_,
      |        candidate0_.military_service as militar71_1_0_,
      |        candidate0_.military_start as militar72_1_0_,
      |        candidate0_.military_wanted as militar73_1_0_,
      |        candidate0_.nationality_id as nation139_1_0_,
      |        candidate0_.partner_candidate_id as partne140_1_0_,
      |        candidate0_.partner_citizenship_id as partne141_1_0_,
      |        candidate0_.partner_edu_level_id as partne142_1_0_,
      |        candidate0_.partner_edu_level_notes as partner74_1_0_,
      |        candidate0_.partner_english as partner75_1_0_,
      |        candidate0_.partner_english_level_id as partne143_1_0_,
      |        candidate0_.partner_ielts as partner76_1_0_,
      |        candidate0_.partner_ielts_score as partner77_1_0_,
      |        candidate0_.partner_ielts_yr as partner78_1_0_,
      |        candidate0_.partner_occupation_id as partne144_1_0_,
      |        candidate0_.partner_occupation_notes as partner79_1_0_,
      |        candidate0_.partner_ref as partner80_1_0_,
      |        candidate0_.partner_registered as partner81_1_0_,
      |        candidate0_.phone as phone82_1_0_,
      |        candidate0_.rego_ip as rego_ip83_1_0_,
      |        candidate0_.rego_partner_param as rego_pa84_1_0_,
      |        candidate0_.rego_referrer_param as rego_re85_1_0_,
      |        candidate0_.rego_utm_campaign as rego_ut86_1_0_,
      |        candidate0_.rego_utm_content as rego_ut87_1_0_,
      |        candidate0_.rego_utm_medium as rego_ut88_1_0_,
      |        candidate0_.rego_utm_source as rego_ut89_1_0_,
      |        candidate0_.rego_utm_term as rego_ut90_1_0_,
      |        candidate0_.resettle_third as resettl91_1_0_,
      |        candidate0_.resettle_third_status as resettl92_1_0_,
      |        candidate0_.residence_status as residen93_1_0_,
      |        candidate0_.residence_status_notes as residen94_1_0_,
      |        candidate0_.return_home_future as return_95_1_0_,
      |        candidate0_.return_home_safe as return_96_1_0_,
      |        candidate0_.return_home_when as return_97_1_0_,
      |        candidate0_.returned_home as returne98_1_0_,
      |        candidate0_.returned_home_reason as returne99_1_0_,
      |        candidate0_.returned_home_reason_no as return100_1_0_,
      |        candidate0_.sflink as sflink101_1_0_,
      |        candidate0_.shareable_cv_attachment_id as sharea145_1_0_,
      |        candidate0_.shareable_doc_attachment_id as sharea146_1_0_,
      |        candidate0_.shareable_notes as sharea102_1_0_,
      |        candidate0_.state as state103_1_0_,
      |        candidate0_.status as status104_1_0_,
      |        candidate0_.survey_comment as survey105_1_0_,
      |        candidate0_.survey_type_id as survey147_1_0_,
      |        candidate0_.text_search_id as text_s106_1_0_,
      |        candidate0_.unhcr_consent as unhcr_107_1_0_,
      |        candidate0_.unhcr_file as unhcr_108_1_0_,
      |        candidate0_.unhcr_not_reg_status as unhcr_109_1_0_,
      |        candidate0_.unhcr_notes as unhcr_110_1_0_,
      |        candidate0_.unhcr_number as unhcr_111_1_0_,
      |        candidate0_.unhcr_status as unhcr_112_1_0_,
      |        candidate0_.unrwa_file as unrwa_113_1_0_,
      |        candidate0_.unrwa_not_reg_status as unrwa_114_1_0_,
      |        candidate0_.unrwa_notes as unrwa_115_1_0_,
      |        candidate0_.unrwa_number as unrwa_116_1_0_,
      |        candidate0_.unrwa_registered as unrwa_117_1_0_,
      |        candidate0_.user_id as user_i148_1_0_,
      |        candidate0_.videolink as videol118_1_0_,
      |        candidate0_.visa_issues as visa_i119_1_0_,
      |        candidate0_.visa_issues_notes as visa_i120_1_0_,
      |        candidate0_.visa_reject as visa_r121_1_0_,
      |        candidate0_.visa_reject_notes as visa_r122_1_0_,
      |        candidate0_.whatsapp as whatsa123_1_0_,
      |        candidate0_.work_abroad as work_a124_1_0_,
      |        candidate0_.work_abroad_notes as work_a125_1_0_,
      |        candidate0_.work_desired as work_d126_1_0_,
      |        candidate0_.work_desired_notes as work_d127_1_0_,
      |        candidate0_.work_permit as work_p128_1_0_,
      |        candidate0_.work_permit_desired as work_p129_1_0_,
      |        candidate0_.work_permit_desired_notes as work_p130_1_0_,
      |        candidate0_.year_of_arrival as year_o131_1_0_,
      |        (SELECT
      |            COUNT(cd.id)
      |        FROM
      |            candidate c
      |        inner join
      |            candidate_dependant cd
      |                on c.id = cd.candidate_id
      |        where
      |            c.id = candidate0_.id
      |        group by
      |            c.id) as formula1_0_,
      |        user1_.created_by as created20_49_1_,
      |        user1_.created_date as created_2_49_1_,
      |        user1_.updated_by as updated21_49_1_,
      |        user1_.updated_date as updated_3_49_1_,
      |        user1_.approver_id as approve22_49_1_,
      |        user1_.email as email4_49_1_,
      |        user1_.first_name as first_na5_49_1_,
      |        user1_.job_creator as job_crea6_49_1_,
      |        user1_.last_login as last_log7_49_1_,
      |        user1_.last_name as last_nam8_49_1_,
      |        user1_.mfa_secret as mfa_secr9_49_1_,
      |        user1_.partner_id as partner23_49_1_,
      |        user1_.password_enc as passwor10_49_1_,
      |        user1_.password_updated_date as passwor11_49_1_,
      |        user1_.purpose as purpose12_49_1_,
      |        user1_.read_only as read_on13_49_1_,
      |        user1_.reset_token as reset_t14_49_1_,
      |        user1_.reset_token_issued_date as reset_t15_49_1_,
      |        user1_.role as role16_49_1_,
      |        user1_.status as status17_49_1_,
      |        user1_.username as usernam18_49_1_,
      |        user1_.using_mfa as using_m19_49_1_,
      |        partnerimp2_.abbreviation as abbrevia2_30_2_,
      |        partnerimp2_.auto_assignable as auto_ass3_30_2_,
      |        partnerimp2_.default_contact_id as default16_30_2_,
      |        partnerimp2_.default_job_creator as default_4_30_2_,
      |        partnerimp2_.default_partner_ref as default_5_30_2_,
      |        partnerimp2_.default_source_partner as default_6_30_2_,
      |        partnerimp2_.job_creator as job_crea7_30_2_,
      |        partnerimp2_.logo as logo8_30_2_,
      |        partnerimp2_.name as name9_30_2_,
      |        partnerimp2_.notification_email as notific10_30_2_,
      |        partnerimp2_.registration_landing_page as registr11_30_2_,
      |        partnerimp2_.sflink as sflink12_30_2_,
      |        partnerimp2_.source_partner as source_13_30_2_,
      |        partnerimp2_.status as status14_30_2_,
      |        partnerimp2_.website_url as website15_30_2_,
      |        country3_.name as name2_20_3_,
      |        country3_.iso_code as iso_code3_20_3_,
      |        country3_.status as status4_20_3_,
      |        country4_.name as name2_20_4_,
      |        country4_.iso_code as iso_code3_20_4_,
      |        country4_.status as status4_20_4_,
      |        educationl5_.name as name2_21_5_,
      |        educationl5_.education_type as educatio3_21_5_,
      |        educationl5_.level as level4_21_5_,
      |        educationl5_.status as status5_21_5_
      |    from
      |        candidate candidate0_
      |    inner join
      |        users user1_
      |            on candidate0_.user_id=user1_.id
      |    inner join
      |        partner partnerimp2_
      |            on user1_.partner_id=partnerimp2_.id
      |    inner join
      |        country country3_
      |            on candidate0_.nationality_id=country3_.id
      |    inner join
      |        country country4_
      |            on candidate0_.country_id=country4_.id
      |    inner join
      |        education_level educationl5_
      |            on candidate0_.max_education_level_id=educationl5_.id
      |    where
      |        (
      |            candidate0_.status in (
      |                'active' , 'unreachable' , 'incomplete' , 'pending'
      |            )
      |        )
      |        and (
      |            user1_.partner_id in (
      |                1 , 3 , 4 , 5 , 6 , 8 , 16
      |            )
      |        )
      |    order by
      |        candidate0_.id desc nulls last limit 20 offset 0
      |""".stripMargin

  def newSearchScreenQuery(): RawSqlActionBuilder =
    jdbc(newSearchScreenQueryName)
      .rawSql(newSearchPaged)

}
