# tekml
###Time Enabled KML

teKML was developed in 2006 with the release of KML version 2.1. One of the new features in 2.1 was the ability to handle begin and end dates. teKML was developed on the GDAL library specifically to transform existing Shapefiles into KML, using designated date fields in the Shapefile attribute table as the BEGIN and END values. In this way, the China Historical GIS time Series spatial objects could be converted into KML placemarks and shown as changing over time with the Play functions in GoogleEarth.

Note: this code was largely rendered obsolete by the Export2KML extension for ArcMap and other conversion tools. This codebase should be considered an artifact.

###Introduction

teKML - Time-Enabled KML Conversion Library - is a Java API and commandline application for converting geodata vector files with time attributes into KML with time elements. Geographic data files in many vector formats (including Shapefiles) can be converted with teKML using the ogr2ogr utility program from FWTools.

For geodata source files in formats other than KML, the ogr library from FWTools is used to initially convert to KML. However, the initial ogr conversion process does not reformat the time attributes into KML time elements. teKML subsequently takes KML files (including ogr-converted KML files, ArcMap's Export to KML files, or GoogleEarth produced KML files) and converts them to KML version 2.1, following the KML specification for adding time elements.

The particular conventions of the time attributes such as the identifying labels for the pertinent fields and the time format are specified in a configuration file for a given workflow along with other instructions to teKML.
