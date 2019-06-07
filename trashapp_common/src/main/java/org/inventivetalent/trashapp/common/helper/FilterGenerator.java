package org.inventivetalent.trashapp.common.helper;

import java.util.Date;

// Helper to generate preferences for recycling types
// https://wiki.openstreetmap.org/wiki/Tag:amenity%3Drecycling
public class FilterGenerator {

	static final String FORMAT_PREFERENCE = "<SwitchPreference\n"
			+ "android:title=\"@string/settings_filter_recycling_%s\"\n"
			+ "android:defaultValue=\"true\"\n"
			+ "android:key=\"filter_recycling_%s\"/>";
	static final String FORMAT_STRING     = "<string name=\"settings_filter_recycling_%s\">%s</string>";
	static final String FORMAT_CODE       = "if (preferences.getBoolean(\"filter_recycling_%s\", true)) {\n"
			+ "\ttypes.add(\"%s\");\n"
			+ "}";

	// 06.06.19
	static String input = "\n"
			+ "Tag\tDescription\n"
			+ "recycling:aerosol_cans=yes/no\tHome and DIY (etc.) aerosol cans (e.g. hairspray, spray paint, etc.)\n"
			+ "recycling:animal_waste=yes/no\t\n"
			+ "recycling:aluminium=yes/no\t\n"
			+ "recycling:bags=yes/no\tAll kinds of bags.\n"
			+ "recycling:batteries=yes/no\tHousehold batteries (e.g. D-cell, 9-volt). Does not imply larger batteries are accepted. There are a handful of uses of recycling:car_batteries.\n"
			+ "recycling:belts=yes/no\tAll belts related to clothing.\n"
			+ "recycling:beverage_cartons=yes/no\tCarton package (multi-materials, e.g. Tetra Pak) for drink and food – possible duplicate of more frequently used recycling:cartons; see above\n"
			+ "recycling:bicycles=yes/no\n"
			+ "recycling:books=yes/no\tDonation of books\n"
			+ "recycling:cans=yes/no\t\n"
			+ "recycling:car_batteries=yes/no\tSome facilities will take household batteries but not car batteries\n"
			+ "recycling:cardboard=yes/no\t\n"
			+ "recycling:cartons=yes/no\tPaper based cartons (for drink or food)\n"
			+ "recycling:cds=yes/no\tCompact discs and DVDs\n"
			+ "recycling:chipboard=yes/no\t\n"
			+ "recycling:christmas_trees=yes/no\tPlace where old christmas trees are collected, use with opening_hours=Jan 01-Jan 15 or so.\n"
			+ "recycling:clothes=yes/no\tDonation of clothes, including shoes (e.g. Humana and others)\n"
			+ "Altkleider-Container-HUMANA-Kleidersammlung-grau.jpgHumana-container.jpgAltkleider-Container-HUMANA-Kleidersammlung-weiss.jpg\n"
			+ "\n"
			+ "recycling:coffee_capsules=yes/no\tShows if recycling facilities take coffee capsules\n"
			+ "recycling:computers=yes/no\t\n"
			+ "recycling:cooking_oil=yes/no\t\n"
			+ "recycling:cork=yes/no\t\n"
			+ "recycling:drugs=yes/no\tExpired drugs.\n"
			+ "recycling:electrical_items=yes/no\tLarger electrical items like fridges, freezers, dishwashers, etc. (See also recycling:white_goods below, which tends to mean fridges, freezers, washing machines, dishwashers, ovens in British English; we'd probably choose recycling:electrical_items or recycling:small_appliances for appliances like TVs, vacuum cleaners, microwaves and anything smaller)\n"
			+ "recycling:engine_oil=yes/no\t\n"
			+ "recycling:fluorescent_tubes=yes/no\t\n"
			+ "recycling:foil=yes/no\tKitchen foil/ tin foil\n"
			+ "recycling:furniture=yes/no\tPiece of furniture or furnishing (ES: mueble)\n"
			+ "recycling:gas_bottles=yes/no\tPressurized gas bottles such as those for camping propane and butane. These are often prohibited elsewhere (See Coleman's Green Key program).\n"
			+ "recycling:glass=yes/no\tImplies recycling:glass_bottles=yes, but there's no standard yet for indicating which of the trickier types (drinking glasses, window glass, light bulb glass, tempered glass, borosilicate glass, Pyrex) are accepted\n"
			+ "recycling:glass_bottles=yes/no\tIncludes jars but not any of the trickier types listed above under recycling:glass\n"
			+ "recycling:green_waste=yes/no\tFIXME: What's the difference between green waste and garden waste\n"
			+ "recycling:garden_waste=yes/no\tFIXME: What's the difference between green waste and garden waste\n"
			+ "recycling:hazardous_waste=yes/no\tCommon house hold hazardous waste (paints, chemicals, asbestos).\n"
			+ "recycling:hardcore=yes/no\tPossible duplicate of more frequently used recycling:rubble; see below.\n"
			+ "recycling:low_energy_bulbs=yes/no\t\n"
			+ "recycling:magazines=yes/no\t\n"
			+ "recycling:metal=yes/no\tAll sorts of metal – possible duplicate of more frequently used recycling:scrap_metal; see below.\n"
			+ "recycling:mobile_phones=yes/no\t\n"
			+ "recycling:newspaper=yes/no\t\n"
			+ "recycling:organic=yes/no\tOrganic remains of food that are recycled into biogas or compost\n"
			+ "recycling:paint=yes/no\t\n"
			+ "recycling:pallets=yes/no\tWooden shipping pallets\n"
			+ "recycling:paper=yes/no\t\n"
			+ "recycling:paper_packaging=yes/no\t\n"
			+ "recycling:pens=yes/no\tWriting instruments, usually plastic pens\n"
			+ "recycling:PET=yes/no\tThis usually includes PET (or PETE: Polyethylene terephthalate) bottles\n"
			+ "recycling:plasterboard=yes/no\tThis usually includes all loose plaster, based on Gypsum\n"
			+ "recycling:plastic=yes/no\tIn the UK at least, often centres/containers will distinguish between (broadly) hard plastics (typically ones that shatter), soft plastics (typically bendy food packaging; black food plastic may be an exception), carrier bags, and all other stretchy plastics. Many will accept only a subset of these, so try to pick more specific categories if you can.\n"
			+ "recycling:plastic_bags=yes/no\t\n"
			+ "recycling:plastic_bottles=yes/no\tOften they allow bottles but not other plastic items!\n"
			+ "recycling:plastic_packaging=yes/no\tOther packaging made out of plastic\n"
			+ "recycling:polyester=yes/no\t\n"
			+ "recycling:polystyrene_foam=yes/no\tPolystyrene foam. Can be Styrofoam™, generic extruded polystyrene foam, or generic expanded polystyrene foam.\n"
			+ "recycling:printer_cartridges=yes/no\tSee below for more specific restrictions, not all centres do both ink and toner (e.g. Ross on Wye)\n"
			+ "recycling:printer_toner_cartridges=yes/no\t\n"
			+ "recycling:printer_inkjet_cartridges=yes/no\t\n"
			+ "recycling:rubble=yes/no\tBricks and building rubble; see also recycling:hardcore above.\n"
			+ "recycling:scrap_metal=yes/no\t\n"
			+ "recycling:sheet_metal=yes/no\t\n"
			+ "recycling:small_appliances=yes/no\t\n"
			+ "recycling:small_electrical_appliances=yes/no\tSmall electrical items such as computer mice, keyboards, electric razors, pocket radios, hair dryers, irons, electric kettles, small quantities of electric cables, etc.\n"
			+ "recycling:styrofoam=yes/no\tTypically refers to expanded styrofoam, such as styrofoam box or packing material\n"
			+ "recycling:tyres=yes/no\t\n"
			+ "recycling:tv_monitor=yes/no\t\n"
			+ "recycling:waste=yes/no\tGeneral waste container (black bags) (don't use this if the waste is not recycled, use a tag like amenity=waste_disposal or amenity=waste_basket instead)\n"
			+ "recycling:white_goods=yes/no\tIn British English this only means fridges, freezers, washing machines, dishwashers, ovens; see also recycling:electrical_items and recycling:small_appliances above, which might be better suited for appliances like TVs, vacuum cleaners, microwaves and anything smaller)\n"
			+ "recycling:wood=yes/no\t";

	public static void main(String[] args) {
		StringBuilder preferences = new StringBuilder();
		StringBuilder strings = new StringBuilder();
		StringBuilder code = new StringBuilder();

		String[] lines = input.split("\n");
		for (String line : lines) {
			if (line.contains("recycling:")) {
				String[] split1 = line.split("recycling:");
				String[] split2 = split1[1].split("=");
				String type = split2[0];

				preferences.append('\n');
				preferences.append(String.format(FORMAT_PREFERENCE, type, type));
				preferences.append('\n');

				strings.append(String.format(FORMAT_STRING, type, toCamelCase(type)));
				strings.append('\n');

				code.append(String.format(FORMAT_CODE, type, type));
				code.append('\n');
			}
		}

		System.out.println("<!-- Generated Preferences @ " + new Date().toString() + " -->");
		System.out.println(preferences);
		System.out.println("<!-- /Generated Preferences -->");
		System.out.println("\n\n");

		System.out.println("<!-- Generated Strings @ " + new Date().toString() + " -->");
		System.out.println(strings);
		System.out.println("<!-- /Generated Strings -->");
		System.out.println("\n\n");

		System.out.println("/* Generated Code @ " + new Date().toString() + " */");
		System.out.println(code);
		System.out.println("/* /Generated Code */");
		System.out.println("\n\n");
	}

	public static String toCamelCase(String string) {
		StringBuilder camelBuilder = new StringBuilder();

		String[] split = string.split("_");
		for (String s : split) {
			String upperCase = s.toUpperCase();
			camelBuilder.append(upperCase.charAt(0));
			camelBuilder.append(s.substring(1));
			camelBuilder.append(' ');
		}
		return camelBuilder.toString().trim();
	}

}
