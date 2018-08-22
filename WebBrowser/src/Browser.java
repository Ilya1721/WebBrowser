import javafx.scene.web.*;
import javafx.stage.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.event.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.beans.value.*;
import java.util.*;
import org.w3c.dom.*;

public class Browser extends Application
{
	private Scene scene;
	private VBox main_layout;
	private HBox document_layout;
	private HBox button_layout;
	private TabPane tab_pane;
	private Button update_button;
	private Button back_button;
	private Button forward_button;
	private Button add_tab_button;
	private TextField url_field;
	private WebEngine web_engine;
	private WebView web_view;
	private Screen screen;
	private Rectangle2D screen_bounds;
	private String starting_page;
	private final int width = 800;
	private final int height = 600;
	private final double screen_document_ratio = 6.86;
	private int tabs_index;
	private Vector<String> history;
	private NodeList links;
	private class MyTab 
	{
		private Tab impl_tab;
		private Vector<String> tab_history;
		private int tab_history_index;
		MyTab(String url) 
		{
			impl_tab = new Tab(url.substring(8, url.indexOf('/', 8)));
			tab_history = new Vector<String>();
			tab_history_index = 0;
		}
		private void init_tab(String url) 
		{
			try 
			{
				System.out.println("Init tab");
				impl_tab.setUserData(url);
				++tabs_index;
				System.out.println("Tabs index = " + tabs_index);
				impl_tab.setOnSelectionChanged(new EventHandler<Event>() 
				{
					public void handle(Event event) 
					{
						if(tab_pane.getTabs().contains(impl_tab)) 
						{
							tabs_index = tab_pane.getTabs().indexOf(impl_tab);
						}
						System.out.println("Tabs index after selection changed = " + tabs_index);
						web_engine.load((String) tabs.get(tabs_index).impl_tab.getUserData());
					}
				});
				impl_tab.setOnClosed(new EventHandler<Event>() 
				{
					public void handle(Event event) 
					{
						tabs_index = tabs.indexOf(tab) - 1;
						tab_pane.getTabs().remove(impl_tab);
						tabs.remove(tab);
						if(tab_pane.getTabs().size() > 0) 
						{
							System.out.println("Tabs index after closing tab = " + tabs_index);
							web_engine.load((String) tab_pane.getTabs().get(tabs_index).getUserData());
						}
						else 
						{
							//not done yet
							System.out.println("Closing");
							Platform.exit();
						}
					}
				});
			}
			catch(Exception exc) 
			{
				System.out.println("Error at init tab");
				System.out.println(exc.toString());
			}
		}
	}
	private MyTab tab;
	private Vector<MyTab> tabs;
	
	public void start(Stage my_stage) 
	{
		init_web();
		init_util();
		init_ui();
		set_buttons();
		set_events();
		set_window_events(my_stage);
		load_starting_page();
		
		my_stage.setTitle("Web Browser");
		my_stage.setScene(scene);
		my_stage.setMaximized(true);
		my_stage.show();
	}


	public static void main(String[] args) 
	{
		launch(args);
	}

	private void init_ui() 
	{
		try 
		{
			System.out.println("Init ui");
			update_button = new Button("Update");
			back_button = new Button("Back");
			forward_button = new Button("Forward");
			add_tab_button = new Button("+");
			tab_pane = new TabPane();
			tab = new MyTab(starting_page);
			tabs.add(tab);
			tab_pane.getTabs().add(tab.impl_tab);
			tab.init_tab(starting_page);
			url_field = new TextField();
			main_layout = new VBox();
			button_layout = new HBox();
			document_layout = new HBox();
			web_view.setZoom(1.4);
			document_layout.setPrefHeight(screen_bounds.getHeight()
					/ screen_document_ratio);
			button_layout.setAlignment(Pos.CENTER_LEFT);
			document_layout.setAlignment(Pos.CENTER_LEFT);
			document_layout.getChildren().addAll(add_tab_button, tab_pane);
			button_layout.getChildren().addAll(back_button, forward_button,
					update_button, url_field);
			main_layout.getChildren().addAll(document_layout, button_layout,
					web_view);
			scene = new Scene(main_layout, width, height);
		}
		catch(Exception exc) 
		{
			System.out.println("Error at init ui");
			System.out.println(exc.toString());
		}
	}
	
	private void init_web() 
	{
		try 
		{
			System.out.println("Init web");
			web_view = new WebView();
			web_engine = web_view.getEngine();
		}
		catch(Exception exc) 
		{
			System.out.println("Error at init web");
			System.out.println(exc.toString());
		}
	} 
	
	private void load_starting_page() 
	{
		try 
		{
			System.out.println("Loading starting page");
			web_engine.load(starting_page);
		}
		catch(Exception exc) 
		{
			System.out.println("Error at loading starting page");
			System.out.println(exc.toString());
		}
	}
	
	private void set_buttons() 
	{
		try 
		{
			System.out.println("Setting buttons");
			System.out.println("Setting update button");
			update_button.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					web_engine.reload();
				}
			});
			System.out.println("Setting back button");
			back_button.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					int index = tabs.get(tabs_index).tab_history_index;
					if(index > 0) 
					{
						System.out.println("Moving backward");
						--tabs.get(tabs_index).tab_history_index;
						--index;
						web_engine.load(tabs.get(tabs_index).tab_history.elementAt(index));
					}
				}
			});
			System.out.println("Setting forward button"); 
			forward_button.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					int index = tabs.get(tabs_index).tab_history_index;
					int size = tabs.get(tabs_index).tab_history.size();
					if(index < size - 1) 
					{
						System.out.println("Moving forward");
						++tabs.get(tabs_index).tab_history_index;
						++index;
						web_engine.load(tabs.get(tabs_index).tab_history.elementAt(index));
					}
				}
			});	
			System.out.println("Setting add new tab button");
			add_tab_button.setOnAction(new EventHandler<ActionEvent>() 
			{
				public void handle(ActionEvent event) 
				{
					System.out.println("Adding new tab");
					tab = new MyTab(starting_page);
					tabs.add(tab);
					tab_pane.getTabs().add(tab.impl_tab);
					tab.init_tab(starting_page);
				}
			});
		}
		catch(Exception exc) 
		{
			System.out.println("Error at setting buttons");
			System.out.println(exc.toString());
		}
	}
	
	private void set_events() 
	{
		try 
		{
			System.out.println("Setting events");
			System.out.println("Setting key pressed event");
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() 
			{
				public void handle(KeyEvent event) 
				{
					if(event.getCode() == KeyCode.ENTER) 
					{
						if(url_field.getText() != "") 
						{
							System.out.println("Loading url from url field");
							tabs.get(tabs_index).impl_tab.setUserData(url_field.getText());
							if(url_field.getText().matches("https://.*\\.*"))
							{
								System.out.println("Loading url straight from url field");
								web_engine.load(url_field.getText());
							}
							else 
							{
								System.out.println("Searching url by searching engine");
								web_engine.load(starting_page);
							}
						}
					}
				}
			});
			System.out.println("Setting url listener");
			web_engine.locationProperty().addListener(new ChangeListener<String>() 
			{
				public void changed(ObservableValue observable, String old_value,
						String new_value) 
				{
					System.out.println("Updating tab info");
					System.out.println("Tabs index = " + tabs_index);
					if(!tabs.get(tabs_index).tab_history.contains(new_value)) 
					{
						System.out.println("Adding url to history");
						tabs.get(tabs_index).tab_history.add(new_value);
						tabs.get(tabs_index).tab_history_index = tabs.get(tabs_index).
								tab_history.size() - 1;
						history.add(new_value);
					}
					System.out.println("Setting tab data");
					tabs.get(tabs_index).impl_tab.setUserData(new_value);
					tabs.get(tabs_index).impl_tab.setText(new_value.substring
							(8, new_value.indexOf('/', 8)));
				}
			});
			System.out.println("Setting document listener");
			web_engine.documentProperty().addListener(new ChangeListener<Document>() 
			{
				public void changed(ObservableValue observable, Document old_value,
						Document new_value) 
				{
					links = new_value.getElementsByTagName("a");
					for(int i = 0; i < links.getLength(); ++i) 
					{
						System.out.println("Link to string = " + links.item(i).toString());
					}
				}
			});
		}
		catch(Exception exc) 
		{
			System.out.println("Error at setting events");
			System.out.println(exc.toString());
		}
	}
	
	private void set_window_events(Stage stage)
	{
		try 
		{
			System.out.println("Setting window events");
			System.out.println("Setting closing event");
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() 
			{
				public void handle(WindowEvent event) 
				{
					try 
					{
						System.out.println("Closing");
					}
					catch(Exception exc) 
					{
						System.out.println("Error at closing");
						System.out.println(exc.toString());
					}
				}
			});
			System.out.println("Setting window width listener");
			stage.widthProperty().addListener(new ChangeListener<Number>() 
			{
				public void changed(ObservableValue observable, Number old_value,
						Number new_value) 
				{
					System.out.println("Setting url field and web view width according to window size");
					url_field.setPrefWidth(new_value.doubleValue() - 300);
					web_view.setPrefWidth(new_value.doubleValue());
				}
			});
			System.out.println("Setting window height listener");
			stage.heightProperty().addListener(new ChangeListener<Number>() 
			{
				public void changed(ObservableValue observable, Number old_value, 
						Number new_value) 
				{
					System.out.println("Setting web view height according to window size");
					web_view.setPrefHeight(new_value.doubleValue());
				}
			});
		}
		catch(Exception exc) 
		{
			System.out.println("Error at setting window events");
			System.out.println(exc.toString());
		}
	}
	
	private void init_util() 
	{
		try 
		{
			System.out.println("Init util");
			starting_page = "https://google.com/";
			history = new Vector<String>();
			tabs = new Vector<MyTab>();
			tabs_index = -1;
			screen = Screen.getPrimary();
			screen_bounds = screen.getVisualBounds();
		}
		catch(Exception exc) 
		{
			System.out.println("Error at init util");
			System.out.println(exc.toString());
		}
	}
	
	private boolean is_between(Number a, Number b, Number c) 
	{
		return a.doubleValue() >= b.doubleValue() && a.doubleValue() < c.doubleValue();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}