/*
import scala.swing._ 
import scala.swing.event._ 

class Spreadsheet(val height: Int, val width: Int) extends ScrollPane { 
	val table = new Table(height, width) { 
		rowHeight = 25 
		autoResizeMode = Table.AutoResizeMode.Off 
		showGrid = true 
		gridColor = new java.awt.Color(150, 150, 150) 
	} 
	val rowHeader = new ListView((0 until height) map (_.toString)) { 
		fixedCellWidth = 30 
		fixedCellHeight = table.rowHeight 
	} 
	viewportView = table 
	rowHeaderView = rowHeader 
} 

class SwingApp extends SimpleSwingApplication { 
	def top = new MainFrame { 
		title = "Reactive Swing App" 
		contents = new Spreadsheet(100, 26)
	} 
} 

object HelloSwing {
	def main(args : Array[String]) = {
		new SwingApp().main(args)
	}
}
*/