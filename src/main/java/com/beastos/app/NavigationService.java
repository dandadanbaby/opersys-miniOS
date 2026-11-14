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

/**
 * Manages navigation between BEAST OS module views.
 * Views are lazily created and cached.
 */
public class NavigationService {

    private VBox contentArea;
    private SidebarView sidebar;
    private HeaderBar header;
    private String currentModule = "";

    // View factory registry — populated during initialization
    private final Map<String, Supplier<Node>> viewFactories = new HashMap<>();
    // Cached views
    private final Map<String, Node> viewCache = new HashMap<>();

    public NavigationService() {
        // Register all module view factories
        registerDefaultFactories();
    }

    private void registerDefaultFactories() {
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

    /**
     * Register a custom view factory for a module (used when algorithm views replace stubs).
     */
    public void registerView(String moduleName, Supplier<Node> factory) {
        viewFactories.put(moduleName, factory);
        viewCache.remove(moduleName); // Clear cached stub
    }

    /**
     * Navigate to a module view by name.
     */
    public void navigateTo(String moduleName) {
        if (moduleName.equals(currentModule)) return;
        if (contentArea == null) return;

        currentModule = moduleName;

        // Get or create the view
        Node view = viewCache.computeIfAbsent(moduleName, name -> {
            Supplier<Node> factory = viewFactories.get(name);
            if (factory != null) {
                return factory.get();
            }
            return createModuleStub(name, "Module not yet implemented.", "⏳");
        });

        // Swap content
        contentArea.getChildren().clear();
        VBox.setVgrow(view, Priority.ALWAYS);
        contentArea.getChildren().add(view);

        // Update sidebar and header
        if (sidebar != null) {
            sidebar.setActiveItem(moduleName);
        }
        if (header != null) {
            header.updateBreadcrumb(moduleName);
        }
    }

    /**
     * Creates a styled placeholder view for modules not yet implemented.
     */
    private Node createModuleStub(String title, String description, String icon) {
        javafx.scene.control.Label sectionTag = new javafx.scene.control.Label("OPERATING SYSTEM WORKBENCH");
        sectionTag.getStyleClass().add("content-section-tag");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.getStyleClass().add("content-title");

        javafx.scene.control.Label subtitleLabel = new javafx.scene.control.Label("COMMAND CENTER");
        subtitleLabel.getStyleClass().add("content-subtitle");

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(description);
        descLabel.getStyleClass().add("content-description");

        // Empty state
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
