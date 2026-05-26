BFST2026 — Navigating Map of Denmark under Changing Sea Levels
Group 15, IT University of Copenhagen, Spring 2026
Prerequisites
	•	Java 21 or higher
	•	Maven (or your IDE's built-in build tool)
	•	
How to run
	1	Clone the repository from github.itu.dk
	2	Open the project in your IDE
	3	Build the project using Maven: mvn clean install
	4	Run the App class located in src/main/java/
	5	A file chooser will appear. Navigate to src/resources/hc-osm-files/ and select a dataset. Pressing cancel will load the default Bornholm map.
Supported file formats
	•	.osm — OpenStreetMap data
	•	.hc — Height curve data
	•	.zip — Zipped archive containing either or both of the above

GUI overview
	•	Bottom left — Sea level slider. Controls the simulated sea level.
	•	Bottom right — Scale bar. Updates dynamically with zoom level.
	•	Top left — Vehicle type selector. Choose between car, bike, or on foot.
	•	Top right — Estimated travel time. Displays after a route is calculated.
How to use
Panning — Click and drag the map using the mouse or trackpad.
Zooming — Scroll up to zoom in, scroll down to zoom out.
Sea level — Click and hold the slider in the bottom left corner, drag left or right, and release to update the map.
Vehicle type — Click the dropdown menu in the top left and select car, bike, or on foot.
Route calculation — Click a start point on the map, then click a destination. The shortest available route will be drawn and the estimated travel time shown in the top right corner.


