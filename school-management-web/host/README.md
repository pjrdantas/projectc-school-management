# Host

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.3.13.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.

```bash
ng add @angular-architects/native-federation --project host --port 4200 --type dynamic-host
```

> Estrutura do projeto

```text
C:.
|   bootstrap.ts
|   custom-theme.scss
|   estrutura.txt
|   index.html
|   main.ts
|   styles.scss
|   
L---app
    |   app.config.ts
    |   app.html
    |   app.routes.ts
    |   app.scss
    |   app.spec.ts
    |   app.ts
    |   
    L---pages
        +---home
        |       home.component.html
        |       home.component.scss
        |       home.component.ts
        |       
        L---menu
                menu.html
                menu.scss
                menu.ts



     _                      _                 ____ _     ___
    / \   _ __   __ _ _   _| | __ _ _ __     / ___| |   |_ _|
   / △ \ | '_ \ / _` | | | | |/ _` | '__|   | |   | |    | |
  / ___ \| | | | (_| | |_| | | (_| | |      | |___| |___ | |
 /_/   \_\_| |_|\__, |\__,_|_|\__,_|_|       \____|_____|___|
                |___/



Angular CLI: 20.3.13
Node: 22.21.0
Package Manager: npm 10.9.4
OS: win32 x64


Angular: 20.3.15
... animations, common, compiler, compiler-cli, core, forms
... platform-browser, router

Package                         Version
---------------------------------------
@angular-devkit/architect       0.2003.13
@angular-devkit/build-angular   20.3.13
@angular-devkit/core            20.3.13
@angular-devkit/schematics      20.3.13
@angular/build                  20.3.13
@angular/cdk                    20.2.14
@angular/cli                    20.3.13
@angular/material               20.2.14
@schematics/angular             20.3.13
rxjs                            7.8.2
typescript                      5.9.3

