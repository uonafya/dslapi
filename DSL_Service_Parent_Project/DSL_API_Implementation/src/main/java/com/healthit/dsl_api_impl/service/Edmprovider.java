package com.healthit.dsl_api_impl.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * MOH DSL EDM Defination
 *
 * @author duncan
 */
public class Edmprovider extends CsdlAbstractEdmProvider {

    // Service Namespace
    public static final String NAMESPACE = "OData.MOHDSL";

    // EDM Container
    public static final String CONTAINER_NAME = "MOHDSL";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types Names    
    public static final String ET_CADRE_NAME = "Cadre";
    public static final FullQualifiedName ET_CADRE_FQN = new FullQualifiedName(NAMESPACE, ET_CADRE_NAME);

    public static final String ET_CADRECATEGORY_NAME = "CadreCategory";
    public static final FullQualifiedName ET_CADRECATEGORY_FQN = new FullQualifiedName(NAMESPACE, ET_CADRECATEGORY_NAME);

    public static final String ET_INDICATOR_NAME = "Indicator";
    public static final FullQualifiedName ET_INDICATOR_FQN = new FullQualifiedName(NAMESPACE, ET_INDICATOR_NAME);

    public static final String ET_INDICATORGROUP_NAME = "IndicatorGroup";
    public static final FullQualifiedName ET_INDICATORGROUP_FQN = new FullQualifiedName(NAMESPACE, ET_INDICATORGROUP_NAME);

    public static final String ET_COUNTRY_NAME = "Country";
    public static final FullQualifiedName ET_COUNTRY_FQN = new FullQualifiedName(NAMESPACE, ET_COUNTRY_NAME);

    public static final String ET_COUNTY_NAME = "County";
    public static final FullQualifiedName ET_COUNTY_FQN = new FullQualifiedName(NAMESPACE, ET_COUNTY_NAME);

    public static final String ET_SUBCOUNTY_NAME = "Subcounty";
    public static final FullQualifiedName ET_SUBCOUNTY_FQN = new FullQualifiedName(NAMESPACE, ET_SUBCOUNTY_NAME);

    public static final String ET_WARD_NAME = "Ward";
    public static final FullQualifiedName ET_WARD_FQN = new FullQualifiedName(NAMESPACE, ET_WARD_NAME);

    public static final String ET_FACILITY_NAME = "Facility";
    public static final FullQualifiedName ET_FACILITY_FQN = new FullQualifiedName(NAMESPACE, ET_FACILITY_NAME);

    public static final String ET_COMMODITY_NAME = "Commodity";
    public static final FullQualifiedName ET_COMMODITY_FQN = new FullQualifiedName(NAMESPACE, ET_COMMODITY_NAME);

    // Entity Set Names
    public static final String ES_COUNTRIES_NAME = "Countries";
    public static final String ES_COUNTIES_NAME = "Counties";
    public static final String ES_SUBCOUNTIES_NAME = "Subcounties";
    public static final String ES_WARDS_NAME = "Wards";
    public static final String ES_FACIITIES_NAME = "Facilities";
    public static final String ES_CADRES_NAME = "Cadres";
    public static final String ES_CADRECATEGORIES_NAME = "CadreCategories";
    public static final String ES_INDICATORS_NAME = "Indicators";
    public static final String ES_INDICATORSGROUP_NAME = "IndicatorGroups";
    public static final String ES_COMMODITIES_NAME = "Commodities";

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {

        // this method is called for each EntityType that are configured in the Schema
        CsdlEntityType entityType = null;

        if (entityTypeName.equals(ET_CADRE_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: many-to-one, null not allowed
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("CadreCategory")
                    .setType(ET_CADRECATEGORY_FQN).setNullable(false).setPartner("Cadres");

            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_CADRE_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);

        } else if (entityTypeName.equals(ET_CADRECATEGORY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("Cadres")
                    .setType(ES_CADRES_NAME).setCollection(true).setPartner("CadreCategory");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_CADRECATEGORY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        } else if (entityTypeName.equals(ET_INDICATOR_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName("Description")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("indicatorGroup")
                    .setType(ET_INDICATORGROUP_FQN).setNullable(false).setPartner("indicators");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_INDICATOR_NAME);
            entityType.setProperties(Arrays.asList(id, name, description));
            entityType.setKey(Arrays.asList(propertyRef));
        }////////// ------ start here
        else if (entityTypeName.equals(ET_INDICATORGROUP_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("indicators")
                    .setType(ES_INDICATORS_NAME).setCollection(true).setPartner("indicatorGroup");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_INDICATORGROUP_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        } else if (entityTypeName.equals(ET_COUNTRY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("counties")
                    .setType(ES_COUNTIES_NAME).setCollection(true).setPartner("country");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_COUNTRY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        }
        if (entityTypeName.equals(ET_COUNTY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("country")
                    .setType(ET_COUNTRY_FQN).setNullable(false).setPartner("counties");
            CsdlNavigationProperty navPropSubCounties = new CsdlNavigationProperty().setName("subcounties")
                    .setType(ES_SUBCOUNTIES_NAME).setCollection(false).setPartner("county");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);
            navPropList.add(navPropSubCounties);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_COUNTY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        }
        if (entityTypeName.equals(ET_SUBCOUNTY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("county")
                    .setType(ET_COUNTY_FQN).setNullable(false).setPartner("subcounties");
            CsdlNavigationProperty navPropWards = new CsdlNavigationProperty().setName("wards")
                    .setType(ES_WARDS_NAME).setCollection(true).setPartner("subcounty");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);
            navPropList.add(navPropWards);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_SUBCOUNTY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        }
        if (entityTypeName.equals(ET_WARD_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("subcounty")
                    .setType(ET_SUBCOUNTY_FQN).setNullable(false).setPartner("wards");
            CsdlNavigationProperty navPropFacilities = new CsdlNavigationProperty().setName("facilities")
                    .setType(ES_FACIITIES_NAME).setCollection(true).setPartner("facility");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);
            navPropList.add(navPropFacilities);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_WARD_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        }
        if (entityTypeName.equals(ET_FACILITY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("ward")
                    .setType(ET_WARD_FQN).setNullable(false).setPartner("facilities");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_FACILITY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        }
        if (entityTypeName.equals(ET_COMMODITY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_COMMODITY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
        }

        return entityType;
    }

    @Override
    public CsdlComplexType getComplexType(final FullQualifiedName complexTypeName) {
        CsdlComplexType complexType = null;
//    if (complexTypeName.equals(CT_ADDRESS_FQN)) {
//      complexType = new CsdlComplexType().setName(CT_ADDRESS_NAME)
//          .setProperties(Arrays.asList(
//              new CsdlProperty()
//                  .setName("City")
//                  .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()),
//              new CsdlProperty()
//              .setName("Country")
//              .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())));
//    }
        return complexType;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        CsdlEntitySet entitySet = null;

        if (entityContainer.equals(CONTAINER)) {

            if (entitySetName.equals(ES_COUNTIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_COUNTIES_NAME);
                entitySet.setType(ET_COUNTY_FQN);

                CsdlNavigationProperty navPropSubCounties = new CsdlNavigationProperty().setName("subcounties")
                        .setType(ES_SUBCOUNTIES_NAME).setCollection(false).setPartner("county");

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Countries"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("country"); // the path from entity type to navigation property

                CsdlNavigationPropertyBinding navPropBindingSub = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Subcounties"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("subcounties"); // the path from entity type to navigation property

                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                navPropBindingList.add(navPropBindingSub);

                entitySet.setNavigationPropertyBindings(navPropBindingList);

            } else if (entitySetName.equals(ES_SUBCOUNTIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_SUBCOUNTIES_NAME);
                entitySet.setType(ET_SUBCOUNTY_FQN);
                
                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Counties"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("county"); // the path from entity type to navigation property
                
                CsdlNavigationPropertyBinding navPropBindingWards = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Wards"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("wards"); // the path from entity type to navigation property
                
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                navPropBindingList.add(navPropBindingWards);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_WARDS_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_WARDS_NAME);
                entitySet.setType(ET_WARD_FQN);
                
                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Subcounties"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("subcounty"); // the path from entity type to navigation property
                
                // navigation
                CsdlNavigationPropertyBinding navPropBindingFacilities = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Facilities"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("facilities");
                
                
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                navPropBindingList.add(navPropBindingFacilities);
                
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_FACIITIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_FACIITIES_NAME);
                entitySet.setType(ET_FACILITY_FQN);
                
                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Wards"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("ward"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_CADRES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_CADRES_NAME);
                entitySet.setType(ET_CADRE_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("CadreCategories"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("CadreCategory"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_CADRECATEGORIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_CADRECATEGORIES_NAME);
                entitySet.setType(ET_CADRECATEGORY_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Cadres"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("Cadres"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_INDICATORS_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_INDICATORS_NAME);
                entitySet.setType(ET_INDICATOR_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("IndicatorGroups"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("indicatorGroup"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_INDICATORSGROUP_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_INDICATORSGROUP_NAME);
                entitySet.setType(ET_INDICATORGROUP_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Indicators"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("indicators"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_COMMODITIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_COMMODITIES_NAME);
                entitySet.setType(ET_COMMODITY_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Products"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("Products"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            } else if (entitySetName.equals(ES_COUNTRIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_COUNTRIES_NAME);
                entitySet.setType(ET_COUNTRY_NAME);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Counties"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("counties"); // the path from entity type to navigation property

                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            }

        }

        return entitySet;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

        // This method is invoked when displaying the service document at
        // e.g. http://localhost:8080/DemoService/DemoService.svc
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }

        return null;
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.add(getEntityType(ET_CADRE_FQN));
        entityTypes.add(getEntityType(ET_CADRECATEGORY_FQN));
        entityTypes.add(getEntityType(ET_INDICATOR_FQN));
        entityTypes.add(getEntityType(ET_INDICATORGROUP_FQN));
        entityTypes.add(getEntityType(ET_COUNTY_FQN));
        entityTypes.add(getEntityType(ET_SUBCOUNTY_FQN));
        entityTypes.add(getEntityType(ET_WARD_FQN));
        entityTypes.add(getEntityType(ET_FACILITY_FQN));
        entityTypes.add(getEntityType(ET_COMMODITY_FQN));

        schema.setEntityTypes(entityTypes);

        // add Complex Types
//        List<CsdlComplexType> complexTypes = new ArrayList<CsdlComplexType>();
//        complexTypes.add(getComplexType(CT_ADDRESS_FQN));
//        schema.setComplexTypes(complexTypes);
        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {

        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.add(getEntitySet(CONTAINER, ES_COUNTIES_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_SUBCOUNTIES_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_WARDS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_FACIITIES_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_CADRES_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_CADRECATEGORIES_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_INDICATORS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_INDICATORSGROUP_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_COMMODITIES_NAME));

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }
}
