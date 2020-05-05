/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.docs;

import com.healthit.dslservice.dao.SurveyDao;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 *
 * @author duncan
 */

public class SurveyDaoTest {

    @Before
    public void setUp() {
        
    }

    @Test
    public void testGetSurveySql() throws Exception {
        SurveyDao sDao=new SurveyDao();
        //String sql=sDao.getSurveySql(2, 7320, null, 4;1);
        //String sql=sDao.getSurveySql(2, 7320, null, null);
        //sDao.getSurveyAvailableDimesions(2, 7320);
    }

}
