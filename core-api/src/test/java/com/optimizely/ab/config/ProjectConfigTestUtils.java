/**
 *
 *    Copyright 2016, Optimizely
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.optimizely.ab.config;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.optimizely.ab.config.audience.AndCondition;
import com.optimizely.ab.config.audience.Audience;
import com.optimizely.ab.config.audience.Condition;
import com.optimizely.ab.config.audience.NotCondition;
import com.optimizely.ab.config.audience.OrCondition;
import com.optimizely.ab.config.audience.UserAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Helper class that provides common functionality and resources for testing {@link ProjectConfig}.
 */
public final class ProjectConfigTestUtils {

    private static final ProjectConfig VALID_PROJECT_CONFIG = generateValidProjectConfig();
    private static ProjectConfig generateValidProjectConfig() {
        List<Experiment> experiments = asList(
            new Experiment("223", "etag1", "Running",
                           singletonList("100"),
                           asList(new Variation("276", "vtag1"),
                                  new Variation("277", "vtag2")),
                           Collections.<String, String>emptyMap(),
                           asList(new TrafficAllocation("276", 3500),
                                  new TrafficAllocation("277", 9000)),
                           ""),
            new Experiment("118", "etag2", "Not started",
                           singletonList("100"),
                           asList(new Variation("278", "vtag3"),
                                  new Variation("279", "vtag4")),
                           Collections.<String, String>emptyMap(),
                           asList(new TrafficAllocation("278", 4500),
                                  new TrafficAllocation("279", 9000)),
                           "")
        );

        List<Attribute> attributes = singletonList(new Attribute("134", "browser_type", "185"));

        List<String> singleExperimentId = singletonList("223");
        List<String> multipleExperimentIds = asList("118", "223");
        List<EventType> events = asList(new EventType("971", "clicked_cart", singleExperimentId),
                                        new EventType("098", "Total Revenue", singleExperimentId),
                                        new EventType("099", "clicked_purchase", multipleExperimentIds));

        List<Condition> userAttributes = new ArrayList<Condition>();
        userAttributes.add(new UserAttribute("browser_type", "custom_dimension", "firefox"));

        OrCondition orInner = new OrCondition(userAttributes);

        NotCondition notCondition = new NotCondition(orInner);
        List<Condition> outerOrList = new ArrayList<Condition>();
        outerOrList.add(notCondition);

        OrCondition orOuter = new OrCondition(outerOrList);
        List<Condition> andList = new ArrayList<Condition>();
        andList.add(orOuter);

        AndCondition andCondition = new AndCondition(andList);

        List<Audience> audiences = singletonList(new Audience("100", "not_firefox_users", andCondition));

        Map<String, String> userIdToVariationKeyMap = new HashMap<String, String>();
        userIdToVariationKeyMap.put("testUser1", "e1_vtag1");
        userIdToVariationKeyMap.put("testUser2", "e1_vtag2");

        List<Experiment> randomGroupExperiments = asList(
            new Experiment("301", "group_etag2", "Running",
                           singletonList("100"),
                           asList(new Variation("282", "e2_vtag1"),
                                  new Variation("283", "e2_vtag2")),
                           Collections.<String, String>emptyMap(),
                           asList(new TrafficAllocation("282", 5000),
                                  new TrafficAllocation("283", 10000)),
                           "42"),
            new Experiment("300", "group_etag1", "Running",
                           singletonList("100"),
                           asList(new Variation("280", "e1_vtag1"),
                                  new Variation("281", "e1_vtag2")),
                           userIdToVariationKeyMap,
                           asList(new TrafficAllocation("280", 3000),
                                  new TrafficAllocation("281", 10000)),
                           "42")
        );

        List<Experiment> overlappingGroupExperiments = asList(
            new Experiment("302", "overlapping_etag1", "Running",
                           singletonList("100"),
                           asList(new Variation("284", "e1_vtag1"),
                                  new Variation("285", "e1_vtag2")),
                           userIdToVariationKeyMap,
                           asList(new TrafficAllocation("284", 1500),
                                  new TrafficAllocation("285", 3000)),
                           "43")
        );

        Group randomPolicyGroup = new Group("42", "random",
                                            randomGroupExperiments,
                                            asList(new TrafficAllocation("300", 3000),
                                                   new TrafficAllocation("301", 9000),
                                                   new TrafficAllocation("", 10000)));
        Group overlappingPolicyGroup = new Group("43", "overlapping",
                                                 overlappingGroupExperiments,
                                                 Collections.<TrafficAllocation>emptyList());
        List<Group> groups = asList(randomPolicyGroup, overlappingPolicyGroup);

        return new ProjectConfig("789", "1234", "4", "42", groups, experiments, attributes, events, audiences);
    }

    private static final ProjectConfig NO_AUDIENCE_PROJECT_CONFIG = generateNoAudienceProjectConfig();
    private static ProjectConfig generateNoAudienceProjectConfig() {
        Map<String, String> userIdToVariationKeyMap = new HashMap<String, String>();
        userIdToVariationKeyMap.put("testUser1", "vtag1");
        userIdToVariationKeyMap.put("testUser2", "vtag2");

        List<Experiment> experiments = asList(
            new Experiment("223", "etag1", "Running",
                           Collections.<String>emptyList(),
                           asList(new Variation("276", "vtag1"),
                                  new Variation("277", "vtag2")),
                           userIdToVariationKeyMap,
                           asList(new TrafficAllocation("276", 3500),
                                  new TrafficAllocation("277", 9000)),
                           ""),
            new Experiment("118", "etag2", "Not started",
                           Collections.<String>emptyList(),
                           asList(new Variation("278", "vtag3"),
                                  new Variation("279", "vtag4")),
                           Collections.<String, String>emptyMap(),
                           asList(new TrafficAllocation("278", 4500),
                                  new TrafficAllocation("279", 9000)),
                           "")
        );

        List<Attribute> attributes = singletonList(new Attribute("134", "browser_type", "185"));

        List<String> singleExperimentId = singletonList("223");
        List<String> multipleExperimentIds = asList("118", "223");
        List<EventType> events = asList(new EventType("971", "clicked_cart", singleExperimentId),
                                        new EventType("098", "Total Revenue", singleExperimentId),
                                        new EventType("099", "clicked_purchase", multipleExperimentIds));

        return new ProjectConfig("789", "1234", "4", "42", Collections.<Group>emptyList(), experiments, attributes,
                                 events, Collections.<Audience>emptyList());
    }

    private ProjectConfigTestUtils() { }

    public static String validConfigJson() throws IOException {
        return Resources.toString(Resources.getResource("config/valid-project-config.json"), Charsets.UTF_8);
    }

    public static String noAudienceProjectConfigJson() throws IOException {
        return Resources.toString(Resources.getResource("config/no-audience-project-config.json"), Charsets.UTF_8);
    }

    /**
     * @return the expected {@link ProjectConfig} for the json produced by {@link #validConfigJson()} ()}
     */
    public static ProjectConfig validProjectConfig() {
        return VALID_PROJECT_CONFIG;
    }

    /**
     * @return the expected {@link ProjectConfig} for the json produced by {@link #noAudienceProjectConfigJson()}
     */
    public static ProjectConfig noAudienceProjectConfig() {
        return NO_AUDIENCE_PROJECT_CONFIG;
    }

    /**
     * Asserts that the provided project configs are equivalent.
     */
    public static void verifyProjectConfig(@CheckForNull ProjectConfig actual, @Nonnull ProjectConfig expected) {
        assertNotNull(actual);

        // verify the project-level values
        assertThat(actual.getAccountId(), is(expected.getAccountId()));
        assertThat(actual.getProjectId(), is(expected.getProjectId()));
        assertThat(actual.getVersion(), is(expected.getVersion()));
        assertThat(actual.getRevision(), is(expected.getRevision()));

        verifyGroups(actual.getGroups(), expected.getGroups());
        verifyExperiments(actual.getExperiments(), expected.getExperiments());
        verifyAttributes(actual.getAttributes(), expected.getAttributes());
        verifyEvents(actual.getEventTypes(), expected.getEventTypes());
        verifyAudiences(actual.getAudiences(), expected.getAudiences());
    }

    /**
     * Asserts that the provided experiment configs are equivalent.
     */
    private static void verifyExperiments(List<Experiment> actual, List<Experiment> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            Experiment actualExperiment = actual.get(i);
            Experiment expectedExperiment = expected.get(i);

            assertThat(actualExperiment.getId(), is(expectedExperiment.getId()));
            assertThat(actualExperiment.getKey(), is(expectedExperiment.getKey()));
            assertThat(actualExperiment.getGroupId(), is(expectedExperiment.getGroupId()));
            assertThat(actualExperiment.getStatus(), is(expectedExperiment.getStatus()));
            assertThat(actualExperiment.getAudienceIds(), is(expectedExperiment.getAudienceIds()));
            assertThat(actualExperiment.getUserIdToVariationKeyMap(),
                       is(expectedExperiment.getUserIdToVariationKeyMap()));

            verifyVariations(actualExperiment.getVariations(), expectedExperiment.getVariations());
            verifyTrafficAllocations(actualExperiment.getTrafficAllocation(),
                                     expectedExperiment.getTrafficAllocation());
        }
    }

    /**
     * Asserts that the provided variation configs are equivalent.
     */
    private static void verifyVariations(List<Variation> actual, List<Variation> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            Variation actualVariation = actual.get(i);
            Variation expectedVariation = expected.get(i);

            assertThat(actualVariation.getId(), is(expectedVariation.getId()));
            assertThat(actualVariation.getKey(), is(expectedVariation.getKey()));
        }
    }

    /**
     * Asserts that the provided traffic allocation configs are equivalent.
     */
    private static void verifyTrafficAllocations(List<TrafficAllocation> actual,
                                          List<TrafficAllocation> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            TrafficAllocation actualDistribution = actual.get(i);
            TrafficAllocation expectedDistribution = expected.get(i);

            assertThat(actualDistribution.getEntityId(), is(expectedDistribution.getEntityId()));
            assertThat(actualDistribution.getEndOfRange(), is(expectedDistribution.getEndOfRange()));
        }
    }

    /**
     * Asserts that the provided attributes configs are equivalent.
     */
    private static void verifyAttributes(List<Attribute> actual, List<Attribute> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            Attribute actualAttribute = actual.get(i);
            Attribute expectedAttribute = expected.get(i);

            assertThat(actualAttribute.getId(), is(expectedAttribute.getId()));
            assertThat(actualAttribute.getKey(), is(expectedAttribute.getKey()));
            assertThat(actualAttribute.getSegmentId(), is(expectedAttribute.getSegmentId()));
        }
    }

    /**
     * Asserts that the provided event configs are equivalent.
     */
    private static void verifyEvents(List<EventType> actual, List<EventType> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            EventType actualEvent = actual.get(i);
            EventType expectedEvent = expected.get(i);

            assertThat(actualEvent.getExperimentIds(), is(expectedEvent.getExperimentIds()));
            assertThat(actualEvent.getId(), is(expectedEvent.getId()));
            assertThat(actualEvent.getKey(), is(expectedEvent.getKey()));
        }
    }

    /**
     * Asserts that the provided audience configs are equivalent.
     */
    private static void verifyAudiences(List<Audience> actual, List<Audience> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            Audience actualAudience = actual.get(i);
            Audience expectedAudience = expected.get(i);

            assertThat(actualAudience.getId(), is(expectedAudience.getId()));
            assertThat(actualAudience.getKey(), is(expectedAudience.getKey()));
            assertThat(actualAudience.getConditions(), is(expectedAudience.getConditions()));
            assertThat(actualAudience.getConditions(), is(expectedAudience.getConditions()));
        }
    }

    /**
     * Assert that the provided group configs are equivalent.
     */
    private static void verifyGroups(List<Group> actual, List<Group> expected) {
        assertThat(actual.size(), is(expected.size()));

        for (int i = 0; i < actual.size(); i++) {
            Group actualGroup = actual.get(i);
            Group expectedGroup = expected.get(i);

            assertThat(actualGroup.getId(), is(expectedGroup.getId()));
            assertThat(actualGroup.getPolicy(), is(expectedGroup.getPolicy()));
            verifyTrafficAllocations(actualGroup.getTrafficAllocation(), expectedGroup.getTrafficAllocation());
            verifyExperiments(actualGroup.getExperiments(), expectedGroup.getExperiments());
        }
    }
}
