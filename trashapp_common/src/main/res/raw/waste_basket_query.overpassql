[out:json][timeout:30][bbox:{{bbox}}];
(
  node["amenity"="waste_basket"];
  way["amenity"="waste_basket"];
  relation["amenity"="waste_basket"];
  
  node["amenity"="recycling"]["recycling_type"!="centre"];
  way["amenity"="recycling"]["recycling_type"!="centre"];
  relation["amenity"="recycling"]["recycling_type"!="centre"];

  node["bin"="yes"];
  way["bin"="yes"];
  relation["bin"="yes"];
);
out body;
>;
out skel qt;