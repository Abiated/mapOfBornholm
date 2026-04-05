package dk.itu.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import dk.itu.parser.interfaces.IParser;
import dk.itu.parser.util.models.ParserResult;
import dk.itu.renderer.osm.Member;
import dk.itu.renderer.osm.OsmElement;
import dk.itu.renderer.osm.OsmNode;
import dk.itu.renderer.osm.OsmRelation;
import dk.itu.renderer.osm.OsmWay;
import dk.itu.renderer.utils.DependsOn;
import dk.itu.renderer.utils.DependsOnExtension;

@ExtendWith(DependsOnExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParserTests {

    static final List<ParserResult> parserResults = new ArrayList<>();

    //Build test results before running test suite
    static {
        try {
            //parserResults.add(new ParserResult("tunoe.osm", "tunoe.json"));
            parserResults.add(new ParserResult("tunoe.osm", "tunoe.json"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError("An exception occurred while creating parser");
        }
    }

    @BeforeEach
    public void Setup() {
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(1)
    void ParserCreatesNodes(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmNode> expectedNodes = expectedParser.getOsmNodeMap();
            HashMap<Long, OsmNode> actualNodes = actualParser.getOsmNodeMap();

            //Assert
            assertEquals(expectedNodes.size(), actualNodes.size(), () -> String.format("[ERROR - %s] : Parser returned OsmNodeMap with %d nodes, expected %d", pr.getName(), actualNodes.size(), expectedNodes.size()));
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(2)
    @DependsOn("ParserCreatesNodes")
    void OsmNodeIdsAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmNode> expectedNodes = expectedParser.getOsmNodeMap();
            HashMap<Long, OsmNode> actualNodes = actualParser.getOsmNodeMap();

            //Assert
            for (Long nodeId : expectedNodes.keySet()) {
                assertTrue(actualNodes.containsKey(nodeId), () -> String.format("[ERROR - %s] : Node #%d is not found in OsmNodeMap", pr.getName(), nodeId));

                OsmNode refNode = actualNodes.get(nodeId);
                assertEquals(nodeId, refNode.getId(), () -> String.format("[ERROR - %s] : ID mismatch between key-value pair in OsmNodeMap. Key %d mapped to object with id %d", pr.getName(), nodeId, refNode.getId()));
            }

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(3)
    @DependsOn("OsmNodeIdsAreCorrect")
    public void OsmNodeCoordinatesAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmNode> expectedNodes = expectedParser.getOsmNodeMap();
            HashMap<Long, OsmNode> actualNodes = actualParser.getOsmNodeMap();

            //Assert
            for (long nodeId : expectedNodes.keySet()) {
                OsmNode expected = expectedNodes.get(nodeId);
                OsmNode actual = actualNodes.get(nodeId);

                assertEquals(expected.getLat(), actual.getLat(), () -> String.format("[ERROR - %s] : Node #%d has latitude %f, should be %f", pr.getName(), expected.getId(), actual.getLat(), expected.getLat()));
                assertEquals(expected.getLon(), actual.getLon(), () -> String.format("[ERROR - %s] : Node #%d has longitude %f, should be %f", pr.getName(), expected.getId(), actual.getLon(), expected.getLon()));
            }
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(4)
    void ParserCreatesWays(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmWay> expectedWays = expectedParser.getOsmWayMap();
            HashMap<Long, OsmWay> actualWays = actualParser.getOsmWayMap();

            //Assert
            assertEquals(expectedWays.size(), actualWays.size(), () -> String.format("[ERROR - %s] : Parser returned OsmWayMap with %d ways, should be %d", pr.getName(), actualWays.size(), expectedWays.size()));
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(5)
    @DependsOn("ParserCreatesWays")
    public void OsmWayIdsAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmWay> expectedWays = expectedParser.getOsmWayMap();
            HashMap<Long, OsmWay> actualWays = actualParser.getOsmWayMap();

            //Assert
            for (Map.Entry<Long, OsmWay> e : expectedWays.entrySet()) {
                assertTrue(actualWays.containsKey(e.getKey()), () -> String.format("[ERROR - %s] : Key %d was not found in OsmWayMap", pr.getName(), e.getKey()));

                OsmWay actual = actualWays.get(e.getKey());
                assertEquals(e.getKey(), actual.getId(), () -> String.format("[ERROR - %s] : ID mismatch between key-value pair in OsmWayMap. Key %d mapped to object with id %d", pr.getName(), e.getKey(), actual.getId()));
            }

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(6)
    @DependsOn("ParserCreatesWays")
    public void OsmWayNodeReferencesAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmWay> expectedWays = expectedParser.getOsmWayMap();
            HashMap<Long, OsmWay> actualWays = actualParser.getOsmWayMap();

            //Assert
            for (Map.Entry<Long, OsmWay> e : expectedWays.entrySet()) {
                //Assert that the actual ways have the same node's assigned
                OsmWay actual = actualWays.get(e.getKey());
                Set<Long> actualNodeIds = actual.getNodes().stream().map(OsmNode::getId).collect(Collectors.toSet());

                for (OsmNode node : e.getValue().getNodes()) {
                    assertTrue(actualNodeIds.contains(node.getId()), () -> String.format("[ERROR - %s] : Node #%d is missing on Way #%d", pr.getName(), node.getId(), e.getKey()));
                }
            }

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(7)
    @DependsOn("ParserCreatesWays")
    public void OsmWayTagsAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmWay> expectedWays = expectedParser.getOsmWayMap();
            HashMap<Long, OsmWay> actualWays = actualParser.getOsmWayMap();

            //Assert
            for (OsmWay way : expectedWays.values()) {
                //Assert that each way actually has the correct tags
                HashMap<String, String> expectedTags = way.getTags();
                HashMap<String, String> actualTags = actualWays.get(way.getId()).getTags();

                assertEquals(expectedTags.size(), actualTags.size(), () -> String.format("[ERROR - %s] : Way #%d has %d tag pairs, should be %d", pr.getName(), way.getId(), actualTags.size(), expectedTags.size()));

                //TODO: Assert each key exists in actual
                //Assert each tag-pair is correct
                for (String tag : expectedTags.keySet()) {
                    assertTrue(actualTags.containsKey(tag), () -> String.format("[ERROR - %s] : Tag key '%s' not present on Way #%d", pr.getName(), tag, way.getId()));
                    assertEquals(expectedTags.get(tag), actualTags.get(tag), () -> String.format("[ERROR - %s] : On Way #%d, tag key '%s' has value '%s', should be '%s'", pr.getName(), way.getId(), tag, actualTags.get(tag), expectedTags.get(tag)));
                }
            }
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(8)
    public void ParserCreatesRelations(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmRelation> expectedRelations = expectedParser.getOsmRelationMap();
            HashMap<Long, OsmRelation> actualRelations = actualParser.getOsmRelationMap();

            //Assert
            assertEquals(expectedRelations.size(), actualRelations.size(), () -> String.format("[ERROR - %s] : Parser returned OsmRelationMap with size %d, should be %d", pr.getName(), actualRelations.size(), expectedRelations.size()));
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(9)
    @DependsOn("ParserCreatesRelations")
    public void OsmRelationIdsAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmRelation> expectedRelations = expectedParser.getOsmRelationMap();
            HashMap<Long, OsmRelation> actualRelations = actualParser.getOsmRelationMap();

            //Assert
            for (OsmRelation expected : expectedRelations.values()) {
                assertTrue(actualRelations.containsKey(expected.getId()), () -> String.format("[ERROR - %s] : Relation #%d was missing in OsmRelationMap", pr.getName(), expected.getId()));

                OsmRelation actual = actualRelations.get(expected.getId());
                assertEquals(expected.getId(), actual.getId(), () -> String.format("[ERROR - %s] : ID mismatch between key-value pair in OsmRelationMap. Key %d mapped to object with id %d", pr.getName(), expected.getId(), actual.getId()));
            }

        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(10)
    @DependsOn("ParserCreatesRelations")
    public void OsmRelationTagsAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmRelation> expectedRelations = expectedParser.getOsmRelationMap();
            HashMap<Long, OsmRelation> actualRelations = actualParser.getOsmRelationMap();

            //Assert
            for (OsmRelation expectedRelation : expectedRelations.values()) {
                OsmRelation actualRelation = actualRelations.get(expectedRelation.getId());

                //Assert relation has correct amount of tags
                HashMap<String, String> expectedTags = expectedRelation.getTags();
                HashMap<String, String> actualTags = actualRelation.getTags();
                assertEquals(expectedTags.size(), actualTags.size(), () -> String.format("[ERROR - %s] : Relation #%d has %d tag pairs, should be %d", pr.getName(), actualRelation.getId(), actualTags.size(), expectedTags.size()));

                //Assert relation has correct tag-keys
                for (String tag : expectedTags.keySet()) {
                    assertTrue(actualTags.containsKey(tag), () -> String.format("[ERROR - %s] : Relation #%d did not contain tag '%s'", pr.getName(), actualRelation.getId(), tag));
                    assertEquals(expectedTags.get(tag), actualTags.get(tag), () -> String.format("[ERROR - %s] : On relation #%d, tag key '%s' has value '%s', should be '%s'", pr.getName(), actualRelation.getId(), tag, actualTags.get(tag), expectedTags.get(tag)));
                }
            }
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(11)
    @DependsOn("ParserCreatesRelations")
    public void OsmRelationMembersAreCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser expectedParser = pr.getExpectedParser();
            IParser actualParser = pr.getActualParser();

            //Act
            HashMap<Long, OsmRelation> expectedRelations = expectedParser.getOsmRelationMap();
            HashMap<Long, OsmRelation> actualRelations = actualParser.getOsmRelationMap();

            //Assert
            for (OsmRelation expectedRelation : expectedRelations.values()) {
                OsmRelation actualRelation = actualRelations.get(expectedRelation.getId());

                //Assert relation has correct members
                List<Member> expectedMembers = expectedRelation.getMembers();
                List<Member> actualMembers = actualRelation.getMembers();

                assertEquals(expectedMembers.size(), actualMembers.size(), () -> String.format("[ERROR - %s] : Relation #%d has %d members, should be %d", pr.getName(), actualRelation.getId(), actualMembers.size(), expectedMembers.size()));

                for (Member member : expectedMembers) {
                    OsmElement memberElement = member.getElement();

                    assertTrue(actualMembers.stream().anyMatch(p -> p.getElement().getId() == memberElement.getId()), () -> String.format("[ERROR - %s] : Relation #%d did not contain element #%d with role '%s' as member", pr.getName(), actualRelation.getId(), memberElement.getId(), member.getRole()));

                    String expectedRole = member.getRole();
                    String actualRole = actualMembers.stream().filter(p -> p.getElement().getId() == memberElement.getId()).findFirst().get().getRole();

                    assertEquals(expectedRole, actualRole, () -> String.format("[ERROR - %s] : On relation #%d, member with element ID #%d has role '%s', should be '%s'", pr.getName(), actualRelation.getId(), memberElement.getId(), actualRole, expectedRole));
                }
            }
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    @ParameterizedTest
    @MethodSource(providerKey)
    @Order(12)
    void BoundingBoxIsCorrect(ParserResult pr) {
        try {
            //Arrange
            IParser actualParser = pr.getActualParser();
            IParser expectedParser = pr.getExpectedParser();

            List<Float> actualBounds = actualParser.getBoundingBox();
            List<Float> expectedBounds = expectedParser.getBoundingBox();

            assertEquals(expectedBounds.size(), actualBounds.size(), () -> String.format("[ERROR - %s] : Size of BoundingBox list did not match. Expected %d values, found %d", pr.getName(), expectedBounds.size(), actualBounds.size()));

            Float expectedMinLat = expectedBounds.get(0);
            Float expectedMinLon = expectedBounds.get(1);
            Float expectedMaxLat = expectedBounds.get(2);
            Float expectedMaxLon = expectedBounds.get(3);
            Float actualMinLat = actualBounds.get(0);
            Float actualMinLon = actualBounds.get(1);
            Float actualMaxLat = actualBounds.get(2);
            Float actualMaxLon = actualBounds.get(3);

            //Act
            //Assert
            //TODO: Make note in PDF that trailing zeroes can safely be removed from Bounding box coordinates (or any coordinates?)
            assertEquals(expectedMinLat, actualMinLat, () -> String.format("[ERROR - %s] : Minimum latitude should be %f, was %f", pr.getName(), expectedMinLat, actualMinLat));
            assertEquals(expectedMaxLat, actualMaxLat, () -> String.format("[ERROR - %s] : Maximum latitude should be %f, was %f", pr.getName(), expectedMaxLat, actualMaxLat));
            assertEquals(expectedMinLon, actualMinLon, () -> String.format("[ERROR - %s] : Minimum longitude should be %f, was %f", pr.getName(), expectedMinLon, actualMinLon));
            assertEquals(expectedMaxLon, actualMaxLon, () -> String.format("[ERROR - %s] : Maximum longitude should be %f, was %f", pr.getName(), expectedMaxLon, actualMaxLon));
        } catch (Exception e) {
            fail(() -> String.format("[EXCEPTION - %s] : %s", pr.getName(), e.getMessage()));
        }
    }

    /// Auxiliary stream provider to feed test data
    final String providerKey = "parserResultProvider";

    @SuppressWarnings("unused")
    static Stream<ParserResult> parserResultProvider() {
        return parserResults.stream();
    }
}
