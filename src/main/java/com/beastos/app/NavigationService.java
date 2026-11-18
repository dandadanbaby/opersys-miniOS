package com.beastos.app;

import com.beastos.banker.view.BankerView;
import com.beastos.cpu.view.CpuSchedulingView;
import com.beastos.dashboard.DashboardView;
import com.beastos.disk.view.DiskSchedulingView;
import com.beastos.fragmentation.view.FragmentationView;
import com.beastos.multiprogramming.view.MultiprogrammingView;
import com.beastos.paging.view.PagingView;
import com.beastos.pagereplacement.view.PageReplacementView;
import com.beastos.segmentation.view.SegmentationView;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/*
 * This is our navigation service that controls switching between all the different OS modules!
 * Basically what happens here is lazy loading: we figured out early on that creating every single
 * view right when the app boots up makes startup super sluggish and eats up tons of memory.
 * So instead, we hold factory suppliers and cache views once they're loaded. Switching back and
 * forth between modules stays fast because we only construct each screen once.
 */
public class NavigationService {

    private VBox contentArea;
    private SidebarView sidebar;
    private HeaderBar header;
    private String currentModule = "";

    // maps each module name to a factory supplier so views only instantiate when clicked (lazy loading!)
    private final Map<String, Supplier<Node>> viewFactories = new HashMap<>();
    // once a view is constructed, we stash it here so we don't recreate the whole UI tree on every tab switch
    private final Map<String, Node> viewCache = new HashMap<>();

    public NavigationService() {
        // this is where we hook up all our OS topic views to their factory suppliers
        registerDefaultFactories();
    }

    private void registerDefaultFactories() {
        // we needed the dashboard to have a reference to this service so its quick-access cards can switch tabs too
        viewFactories.put("Overview", () -> {
            DashboardView dv = new DashboardView();
            dv.setNavigationService(this);
            return dv;
        });
        viewFactories.put("CPU Scheduling", CpuSchedulingView::new);
        viewFactories.put("Banker's Algorithm", BankerView::new);
        viewFactories.put("Paging", PagingView::new);
        viewFactories.put("Page Replacement", PageReplacementView::new);
        viewFactories.put("Segmentation", SegmentationView::new);
        viewFactories.put("Fragmentation", FragmentationView::new);
        viewFactories.put("Disk Scheduling", DiskSchedulingView::new);
        viewFactories.put("Multiprogramming", MultiprogrammingView::new);
    }

    // we added this so if we swap out a view or plug in an updated simulator, we can register it dynamically
    public void registerView(String moduleName, Supplier<Node> factory) {
        viewFactories.put(moduleName, factory);
        viewCache.remove(moduleName); // we clear the old cached version so the new factory gets called next time
    }

    // basically our main router method — switches whatever is currently on screen to the selected module
    public void navigateTo(String moduleName) {
        if (moduleName.equals(currentModule)) return;
        if (contentArea == null) return;

        currentModule = moduleName;

        // we check if the view is already cached; if not, invoke the factory or fallback to our placeholder stub
        Node view = viewCache.computeIfAbsent(moduleName, name -> {
            Supplier<Node> factory = viewFactories.get(name);
            if (factory != null) {
                return factory.get();
            }
            return createModuleStub(name, "Module not yet implemented.", "⏳");
        });

        // swap the view into the center container and make sure it expands to fill the full window height
        contentArea.getChildren().clear();
        VBox.setVgrow(view, Priority.ALWAYS);
        contentArea.getChildren().add(view);

        // keep the sidebar active indicator and top breadcrumb title in sync with what's on screen
        if (sidebar != null) {
            sidebar.setActiveItem(moduleName);
        }
        if (header != null) {
            header.updateBreadcrumb(moduleName);
        }
    }

    // fallback placeholder screen we put together while working on other modules
    // that way clicking an unfinished module doesn't crash the app lol, it just shows a nice "coming soon" box
    private Node createModuleStub(String title, String description, String icon) {
        javafx.scene.control.Label sectionTag = new javafx.scene.control.Label("OPERATING SYSTEM WORKBENCH");
        sectionTag.getStyleClass().add("content-section-tag");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.getStyleClass().add("content-title");

        javafx.scene.control.Label subtitleLabel = new javafx.scene.control.Label("COMMAND CENTER");
        subtitleLabel.getStyleClass().add("content-subtitle");

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(description);
        descLabel.getStyleClass().add("content-description");

        // empty state card centered on screen with an icon, title, and hint text
        javafx.scene.control.Label emptyIcon = new javafx.scene.control.Label(icon);
        emptyIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: #64748b;");

        javafx.scene.control.Label emptyText = new javafx.scene.control.Label(
                "Module implementation coming in Phase 2-6");
        emptyText.getStyleClass().add("module-empty-text");

        javafx.scene.control.Label emptyHint = new javafx.scene.control.Label(
                "This workspace is ready for " + title + " algorithms and visualizations.");
        emptyHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        VBox emptyState = new VBox(16, emptyIcon, emptyText, emptyHint);
        emptyState.getStyleClass().add("module-empty-state");
        emptyState.setStyle("-fx-alignment: center;");
        VBox.setVgrow(emptyState, Priority.ALWAYS);

        VBox header = new VBox(2, sectionTag, titleLabel, subtitleLabel);
        header.setStyle("-fx-padding: 0 0 8 0;");

        VBox container = new VBox(8, header, descLabel, emptyState);
        VBox.setVgrow(container, Priority.ALWAYS);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return scrollPane;
    }

    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    public void setSidebar(SidebarView sidebar) {
        this.sidebar = sidebar;
    }

    public void setHeader(HeaderBar header) {
        this.header = header;
    }

    public String getCurrentModule() {
        return currentModule;
    }
}
