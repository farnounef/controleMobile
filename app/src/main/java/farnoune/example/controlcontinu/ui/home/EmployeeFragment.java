package farnoune.example.controlcontinu.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import farnoune.example.controlcontinu.R;

public class EmployeeFragment extends Fragment {

    private EditText name;
    private Button bnAdd;
    private String insertUrl = "http://10.0.2.2:8088/api/v1/employees";
    private String listUrl = "http://10.0.2.2:8088/api/v1/employees";
    LinearLayout employeeListLayout;

    private String selectedEmployeeId = null;
    private EditText editName;

    private void fetchDataAndPopulateList() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, listUrl, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                employeeListLayout.removeAllViews();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject employeeObject = response.getJSONObject(i);
                        String employeeId = employeeObject.getString("id");
                        String name = employeeObject.getString("nom");
                        View listItemView = LayoutInflater.from(requireContext()).inflate(R.layout.list_item_employee, null);

                        TextView nameTextView = listItemView.findViewById(R.id.NameTextView);

                        nameTextView.setText(name);

                        listItemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showOptionsDialog(employeeId);
                            }
                        });

                        employeeListLayout.addView(listItemView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee, container, false);

        name = view.findViewById(R.id.name);
        bnAdd = view.findViewById(R.id.bnAdd);
        employeeListLayout = view.findViewById(R.id.employeeListLayout);

        bnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameText = name.getText().toString();

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("nom", nameText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                        insertUrl, jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("resultat", response + "");

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                builder.setMessage("Ajout avec succès")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                name.setText("");
                                                fetchDataAndPopulateList();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

                requestQueue.add(request);
            }
        });

        fetchDataAndPopulateList();

        return view;
    }

    private void showOptionsDialog(String employeeId) {
        selectedEmployeeId = employeeId;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setTitle("Options");

        dialogBuilder.setPositiveButton("Modifier", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSimpleEditPopup(employeeId);
            }
        });

        dialogBuilder.setNegativeButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showConfirmationDialog(employeeId);
            }
        });

        dialogBuilder.show();
    }

    private void showSimpleEditPopup(String employeeId) {
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(requireContext());

        View editView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_layout_employee, null);
        editName = editView.findViewById(R.id.editName);

        fetchDataForEmployee(employeeId);

        editDialogBuilder.setView(editView);

        editDialogBuilder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedName = editName.getText().toString();

                updateEmployee(employeeId, updatedName);
            }
        });

        editDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        editDialogBuilder.show();
    }

    private void fetchDataForEmployee(String employeeId) {
        String employeeDataUrl = "http://10.0.2.2:8088/api/v1/employees/" + employeeId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, employeeDataUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("nom");


                    editName.setText(name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }

    private void updateEmployee(String employeeId, String updatedName) {
        String updateUrl = "http://10.0.2.2:8088/api/v1/employees/" + employeeId;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nom", updatedName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, updateUrl, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showSuccessMessage("Modification réussie");
                fetchDataAndPopulateList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(request);
    }

    private void showConfirmationDialog(String employeeId) {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(requireContext());
        confirmDialogBuilder.setMessage("Voulez-vous vraiment supprimer cet élément ?");
        confirmDialogBuilder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEmployee(employeeId);
            }
        });

        confirmDialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        confirmDialogBuilder.show();
    }

    private void deleteEmployee(String employeeId) {
        String deleteUrl = "http://10.0.2.2:8088/api/v1/employees/" + employeeId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showSuccessMessage("Suppression réussie");
                fetchDataAndPopulateList();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        requestQueue.add(deleteRequest);
    }

    private void showSuccessMessage(String message) {
        AlertDialog.Builder successDialogBuilder = new AlertDialog.Builder(requireContext());
        successDialogBuilder.setMessage(message);
        successDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fetchDataAndPopulateList();
                dialog.dismiss();
            }
        });

        AlertDialog successDialog = successDialogBuilder.create();
        successDialog.show();
    }
}
