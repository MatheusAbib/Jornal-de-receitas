package com.receitas.site_receitas.controller;

import com.receitas.site_receitas.model.Receita;
import com.receitas.site_receitas.repository.ReceitaRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
    public class ReceitaController {

    private final ReceitaRepository repository;

    public ReceitaController(ReceitaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("receitas", repository.findByAprovadaTrue());
        return "index"; 
    }

    @GetMapping("/nova")
    public String novaReceitaForm(Model model) {
        model.addAttribute("receita", new Receita());
        return "form";
}

    @PostMapping("/salvar")
    public String salvarReceita(@ModelAttribute Receita receita,
                            @RequestParam("imagemFile") MultipartFile imagemFile,
                            @RequestParam("ingredientes") List<String> ingredientes,
                            @RequestParam("modoPreparo") List<String> modoPreparo) throws Exception {

    receita.setIngredientes(String.join("\n", ingredientes));
    receita.setModoPreparo(String.join("\n", modoPreparo));

    if (!imagemFile.isEmpty()) {
        String uploadDir = "uploads/";
        Files.createDirectories(Paths.get(uploadDir));
        String filename = System.currentTimeMillis() + "_" + imagemFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, imagemFile.getBytes());
        receita.setImagem(filename);
    }

    receita.setAprovada(false);
    repository.save(receita);
    return "redirect:/pendentes";
    }

    @GetMapping("/pendentes")
    public String pendentes(Model model) {
        model.addAttribute("receitas", repository.findByAprovadaFalse());
        return "pendentes"; // criar essa view
    }

    @PostMapping("/aprovar/{id}")
    public String aprovarReceita(@PathVariable Long id) {
        Receita receita = repository.findById(id).orElse(null);
        if (receita != null) {
            receita.setAprovada(true);
            repository.save(receita);
        }
        return "redirect:/pendentes";
    }

    @PostMapping("/rejeitar/{id}")
    public String rejeitarReceita(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/pendentes";
    }

    @GetMapping("/detalhe/{id}")
    public String detalheReceita(@PathVariable Long id, Model model) {
        Receita receita = repository.findById(id).orElse(null);
        if (receita == null) {
            return "redirect:/"; // se não existir, volta para a página principal
        }
        model.addAttribute("receita", receita);
        return "detalhe"; // renderiza detalhe.html
    }

}