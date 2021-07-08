package com.example.application.views.agendatelefônica;

import com.example.application.data.DAO.PessoaDao;
import com.example.application.data.entity.Pessoa;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
        import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;


import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.application.views.main.MainView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;

import javax.persistence.criteria.CriteriaBuilder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Route(value = "agenda/:pessoaID?/:action?(edit)", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Agenda Telefônica")
public class AgendaTelefônicaView extends Div implements BeforeEnterObserver{

    private final String PESSOA_ID = "pessoaID";
    private final String PESSOA_EDIT_ROUTE_TEMPLATE = "agenda/%d/edit";

    private Grid<Pessoa> grid = new Grid<>(Pessoa.class, false);

    private TextField nome;
    private TextField sobrenome;
    private DatePicker data_nascimento;
    private TextField contato;
    private TextField parentesco;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");
    private Button delete = new Button("Delete");

    private BeanValidationBinder<Pessoa> binder;

    private Pessoa pessoa;


    VerticalLayout verticalLayoutContato;

    public AgendaTelefônicaView() throws SQLException {
        addClassNames("agenda-telefônica-view", "flex", "flex-col", "h-full");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("nome").setAutoWidth(true);
        grid.addColumn("sobrenome").setAutoWidth(true);
        grid.addColumn("data_nascimento").setAutoWidth(true);
        grid.addColumn("parentesco").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PESSOA_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
                PessoaDao pessoaDao = new PessoaDao();
                removerCamposContatos();
                List<Integer> listContatos = pessoaDao.getContatos(event.getValue().getId());
                for (Integer contatos:listContatos){
                    verticalLayoutContato.add(inserirCampos(contatos.toString()));
                }

            } else {
                removerCamposContatos();
                clearForm();
                UI.getCurrent().navigate(AgendaTelefônicaView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Pessoa.class);

        // Bind fields. This where you'd define e.g. validation rules

        binder.bindInstanceFields(this);
        binder.forField(nome)
                .asRequired()
                .withValidator(e -> e.matches("[a-zA-Z]{" + nome.getValue().length()+ "}"),"Apenas letras devem ser digitadas no campo acima")
                .bind(Pessoa::getNome,Pessoa::setNome);

        binder.forField(sobrenome)
                .asRequired()
                .withValidator(e -> e.matches("[a-zA-Z]{" + sobrenome.getValue().length()+ "}"),"Apenas letras devem ser digitadas no campo acima")
                .bind(Pessoa::getSobrenome,Pessoa::setSobrenome);

        binder.forField(data_nascimento)
                .asRequired()
                .withValidator(e -> e.isBefore(LocalDate.now()), "Coloque uma data valida")
                .bind(Pessoa::getData_nascimento,Pessoa::setData_nascimento);


        binder.forField(parentesco)
                .asRequired()
                .withValidator(e -> e.matches("[a-zA-Z]{" + parentesco.getValue().length()+ "}"),"Apenas letras devem ser digitadas no campo acima")
                .bind(Pessoa::getSobrenome,Pessoa::setSobrenome);



        cancel.addClickListener(e -> {
            clearForm();
            removerCamposContatos();
            popularGrid();
        });

        delete.addClickListener(e ->{
            if(pessoa == null){
                Notification.show("Nenhum cadastro selecionado");
                return;
            }

            PessoaDao dao = new PessoaDao();

            if(dao.deleteContato(pessoa) && dao.delete(pessoa)){
                Notification.show("Deletado");
                System.out.println("Deletado");
            }else{
                Notification.show("Não Deletado");
                System.out.println("Não Deletado");
            }
            clearForm();
            removerCamposContatos();
            popularGrid();
        });

        save.addClickListener(e -> {
            if (binder.validate().isOk()){
                try {
                    if (this.pessoa == null) {
                        this.pessoa = new Pessoa();
                    }

                    binder.writeBean(this.pessoa);

                    PessoaDao dao = new PessoaDao();

                    if(pessoa.getId() == null){
                        if(dao.add(pessoa) && dao.addContato(getListContatos())){
                            Notification.show("Cadastrado");
                            System.out.println("Cadastrado");
                            removerCamposContatos();
                        }else{
                            Notification.show("Não Cadastrado");
                            System.out.println("Não Cadastrado");
                        }
                    }else{
                        if(dao.update(pessoa) && dao.updateContato(pessoa.getId(), getListContatos())){
                            Notification.show("Alterado");
                            System.out.println("Alterado");
                            removerCamposContatos();
                        }else{
                            Notification.show("Não Alterado");
                            System.out.println("Não Alterado");
                        }
                    }
                    UI.getCurrent().navigate(AgendaTelefônicaView.class);
                } catch (ValidationException  validationException) {
                    Notification.show("An exception happened while trying to store the pessoa details.");
                }
                popularGrid();
            }else{
                Notification.show("Preencha os campos corretamente");
            }


        });
        popularGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> pessoaId = event.getRouteParameters().getInteger(PESSOA_ID);
        PessoaDao pessoaDao = new PessoaDao();
        if (pessoaId.isPresent()) {
            Optional<Pessoa> pessoaFromBackend = pessoaDao.getIdPessoa(pessoaId.get());
            if (pessoaFromBackend.isPresent()) {
                populateForm(pessoaFromBackend.get());
            } else {
                Notification.show(String.format("The requested pessoa was not found, ID = %d", pessoaId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                popularGrid();
                event.forwardTo(AgendaTelefônicaView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        nome = new TextField("Nome");
        sobrenome = new TextField("Sobrenome");
        data_nascimento = new DatePicker("Data_nascimento");
        parentesco = new TextField("Parentesco");

        Component[] fields = new Component[]{nome, sobrenome, data_nascimento, parentesco};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }

        verticalLayoutContato = new VerticalLayout();
        verticalLayoutContato.add();
        verticalLayoutContato.setSpacing(false);
        verticalLayoutContato.setPadding(false);


        Button addNumero = new Button("Adicionar número");
        addNumero.addClickListener(e -> verticalLayoutContato.add(inserirCampos(null)));

        Label labelContato = new Label();
        labelContato.add("Contato(s)");
        labelContato.setWidthFull();

        formLayout.add(fields);
        formLayout.add(labelContato, verticalLayoutContato, addNumero);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    public Component inserirCampos(String text){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        TextField textField = new TextField();
        Button button = new Button("x");
        textField.setWidth(100, Unit.PERCENTAGE);
        button.setWidth(40, Unit.PIXELS);

        if(text != null){
            textField.setValue(text);
        }

        binder.forField(textField)
                .asRequired()
                .withValidator(contato -> contato.matches("[0-9]{"+textField.getValue().length()+"}"), "O contado teve conter apenas números")
                .bind(Pessoa::getContato,Pessoa::setContato);

        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.addClickListener(e -> horizontalLayout.removeAll());

        horizontalLayout.add(textField,button);
        return horizontalLayout;
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void clearForm() {
        populateForm(null);
    }

    private List<String> getListContatos(){
        List<Component> b = verticalLayoutContato.getChildren()
                .filter(component -> component instanceof HorizontalLayout)
                .flatMap(Component::getChildren)
                .collect(Collectors.toList());

        List<String> listContatos = b.stream()
                .filter(component -> component instanceof TextField)
                .map(c -> ((TextField) c).getValue())
                .collect(Collectors.toList());
        return listContatos;
    }

    private void populateForm(Pessoa value) {
        this.pessoa = value;
        binder.readBean(this.pessoa);
    }

    private void removerCamposContatos(){
        verticalLayoutContato.removeAll();
    }

    private void popularGrid(){
        PessoaDao dao = new PessoaDao();
        List<Pessoa> pessoas = dao.GetlList();
        clearForm();
        grid.setItems(pessoas);
    }

    private boolean validaNome(String nome){
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(nome);
        while (matcher.find()){
            if (matcher.matches()){
                return true;
            }
        }
        return false;
    }
}
