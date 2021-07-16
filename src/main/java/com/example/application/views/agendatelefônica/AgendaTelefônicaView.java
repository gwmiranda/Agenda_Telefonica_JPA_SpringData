package com.example.application.views.agendatelefônica;

import com.example.application.data.entity.Contato;
import com.example.application.data.entity.Pessoa;
import com.example.application.data.service.PessoaService;
import com.example.application.views.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    private DatePicker dataNascimento;
    private TextField parentesco;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");
    private Button delete = new Button("Delete");

    private BeanValidationBinder<Pessoa> binderPessoa;
    private BeanValidationBinder<Contato> binderContato;

    VerticalLayout verticalLayoutContato;
    private Pessoa pessoa;

    private final PessoaService pessoaService;

    public AgendaTelefônicaView(PessoaService pessoaService) throws SQLException {
        this.pessoaService = pessoaService;
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
        grid.addColumn("dataNascimento").setAutoWidth(true);
        grid.addColumn("parentesco").setAutoWidth(true);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PESSOA_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
                removerCamposContatos();
                Pessoa pessoa = pessoaService.getPessoaID(event.getValue().getId());
                inserirCampoContatoAutomatico(pessoa);
            } else {
                clearForm();
                removerCamposContatos();
                UI.getCurrent().navigate(AgendaTelefônicaView.class);
            }
        });

        // Configure Form
        binderPessoa = new BeanValidationBinder<>(Pessoa.class);

        // Bind fields. This where you'd define e.g. validation rules
        binderPessoa.bindInstanceFields(this);

        binderPessoa.forField(nome)
                .asRequired()
                .withValidator(e -> e.matches("[a-zA-Zà-úÀ-Ú]{3,20}"),
                        "O campo acima só permite de 3 a 20 caracteres, não pode conter números e espaço vazio.")
                .bind(Pessoa::getNome, Pessoa::setNome);

        binderPessoa.forField(sobrenome)
                .asRequired()
                .withValidator(e -> e.matches("[a-zA-Zà-úÀ-Ú ]{3,35}"),
                        "O campo acima só permite de 3 a 35 caracteres e não pode conter letras.")
                .bind(Pessoa::getSobrenome, Pessoa::setSobrenome);

        binderPessoa.forField(dataNascimento)
                .asRequired()
                .withValidator(e -> e.isBefore(LocalDate.now()),
                        "Coloque apenas datas inferiores a "
                                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .bind(Pessoa::getDataNascimento, Pessoa::setDataNascimento);

        binderPessoa.forField(parentesco)
                .asRequired()
                .withValidator(e -> e.matches("[a-zA-Zà-úÀ-Ú ]{3,25}"),
                        "O campo acima só permite de 3 a 25 caracteres, não pode conter letras e espeços vazios.")
                .bind(Pessoa::getParentesco, Pessoa::setParentesco);;

        //Buttons
        cancel.addClickListener(e -> {
            removerCamposContatos();
            popularGrid();
        });

        delete.addClickListener(e ->{
            if(pessoa == null){
                Notification.show("Nenhum cadastro selecionado");
                return;
            }

            if(pessoaService.deletar(pessoa)){
                Notification.show("Deletado");
            }else{
                Notification.show("Não Deletado");
            }
            removerCamposContatos();
            popularGrid();
        });

        save.addClickListener(e -> {
            if (binderPessoa.validate().isOk()){
                try {
                    if (this.pessoa == null) {
                        this.pessoa = new Pessoa();
                    }

                    binderPessoa.writeBean(this.pessoa);

                    if(pessoa.getId() == null){
                        pessoa.setContato(getListContatos(pessoa));
                        if(pessoaService.salvar(pessoa) ){
                            Notification.show("Cadastrado");
                            removerCamposContatos();
                        }else{
                            Notification.show("Não Cadastrado");
                        }
                    }else{
                        pessoa.setContato(getListContatos(pessoa));
                        if(pessoaService.salvar(pessoa)){
                            Notification.show("Alterado");
                            removerCamposContatos();
                        }else{
                            Notification.show("Não Alterado");
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
        Optional<java.lang.Integer> pessoaId = event.getRouteParameters().getInteger(PESSOA_ID);
        if (pessoaId.isPresent()) {
            Pessoa pessoaFromBackend = pessoaService.getPessoaID(pessoaId.get());

            if (pessoaFromBackend != null){
                populateForm(pessoaFromBackend);
            }
            else {
                Notification.show(String.format("The requested pessoa was not found, ID = %d", pessoaId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                clearForm();
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
        dataNascimento = new DatePicker("Data_nascimento");
        parentesco = new TextField("Parentesco");

        Component[] fields = new Component[]{nome, sobrenome, dataNascimento, parentesco};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }

        verticalLayoutContato = new VerticalLayout();
        verticalLayoutContato.add();
        verticalLayoutContato.setSpacing(false);
        verticalLayoutContato.setPadding(false);


        Button addNumero = new Button("Adicionar número");
        addNumero.addClickListener(e -> verticalLayoutContato.add(inserirCampoContatoManual("")));

        Label labelContato = new Label();
        labelContato.add("Contato(s)");
        labelContato.setWidthFull();

        formLayout.add(fields);
        formLayout.add(labelContato, verticalLayoutContato, addNumero);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    public void inserirCampoContatoAutomatico(Pessoa pessoa){
        for (Contato numero: pessoa.getContato()){
            verticalLayoutContato.add(inserirCampoContatoManual(numero.getContato().toString()));
        }
    }

    public Component inserirCampoContatoManual(String text){
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        TextField textField = new TextField();
        Button button = new Button("x");
        textField.setWidth(100, Unit.PERCENTAGE);
        button.setWidth(40, Unit.PIXELS);
        textField.setValue(text);

        binderContato = new BeanValidationBinder<>(Contato.class);
        binderPessoa.bindInstanceFields(this);
        binderContato.forField(textField)
                .asRequired()
                .withConverter(new StringToIntegerConverter(
                        "O campo acima deve conter apenas números e não deve ter espaços vazios."))
                .bind(Contato::getContato, Contato::setContato);

        button.addThemeVariants(ButtonVariant.LUMO_ERROR);
        button.addClickListener(e -> horizontalLayout.removeAll());

        horizontalLayout.add(textField, button);
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

    private List<Contato> getListContatos(Pessoa pessoa){
        List<Component> b = verticalLayoutContato.getChildren()
                .filter(component -> component instanceof HorizontalLayout)
                .flatMap(Component::getChildren)
                .collect(Collectors.toList());

        List<String> listString = b.stream()
                .filter(component -> component instanceof TextField)
                .map(c -> ((TextField) c).getValue())
                .collect(Collectors.toList());


        List<Contato> listContatos = listString.stream()
                .map(c -> new Contato(java.lang.Integer.parseInt(c)))
                .collect(Collectors.toList());
        return listContatos;
    }

    private void removerCamposContatos(){
        verticalLayoutContato.removeAll();
    }

    private void popularGrid(){
        clearForm();
        List<Pessoa> pessoas = pessoaService.listarPessoas();
        grid.setItems(pessoas);
    }

    private void populateForm(Pessoa value) {
        this.pessoa = value;
        binderPessoa.readBean(this.pessoa);
    }
}
